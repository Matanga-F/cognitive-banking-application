#!/bin/bash

BASE_URL="http://localhost:8080/api/cognitive/bank/cards"

ACCOUNTS_FILE="accounts_created.json"
USERS_FILE="users_created.json"
OUTPUT_FILE="cards_created.json"

CARD_TYPES=("DEBIT" "CREDIT")
DAILY_LIMIT=3000.00
CREDIT_LIMIT=0.00

# Checks
[ ! -f "$ACCOUNTS_FILE" ] && echo "❌ accounts_created.json not found" && exit 1
[ ! -f "$USERS_FILE" ] && echo "❌ users_created.json not found" && exit 1

echo "[" > "$OUTPUT_FILE"
FIRST=true

# Flatten accounts JSON
ACCOUNTS=$(cat "$ACCOUNTS_FILE" | tr -d '\n' | sed 's/},{/}\n{/g' | sed 's/^\[\|\]$//g')

for acc in $ACCOUNTS; do

  USER_ID=$(echo "$acc" | sed -n 's/.*"userId":"\([^"]\+\)".*/\1/p')
  ACCOUNT_ID=$(echo "$acc" | sed -n 's/.*"accountId":"\([^"]\+\)".*/\1/p')

  [ -z "$USER_ID" ] && continue
  [ -z "$ACCOUNT_ID" ] && continue

  # Find full name from users file
  USER_LINE=$(grep "\"userId\":\"$USER_ID\"" "$USERS_FILE")
  FULL_NAME=$(echo "$USER_LINE" | sed -n 's/.*"fullName":"\([^"]\+\)".*/\1/p')

  [ -z "$FULL_NAME" ] && continue

  CARD_TYPE=${CARD_TYPES[$RANDOM % ${#CARD_TYPES[@]}]}

  echo "Creating $CARD_TYPE card for $FULL_NAME"

  RESPONSE=$(curl -s -X POST "$BASE_URL" \
    -H "Content-Type: application/json" \
    -d "{
      \"userId\": \"$USER_ID\",
      \"accountId\": \"$ACCOUNT_ID\",
      \"cardType\": \"$CARD_TYPE\",
      \"cardHolderName\": \"$FULL_NAME\",
      \"creditLimit\": $CREDIT_LIMIT,
      \"dailyLimit\": $DAILY_LIMIT
    }")

  CARD_ID=$(echo "$RESPONSE" | sed -n 's/.*"cardId":"\([^"]\+\)".*/\1/p')

  if [ -z "$CARD_ID" ]; then
    echo "❌ Card creation failed for $FULL_NAME"
    echo "Response: $RESPONSE"
    continue
  fi

  echo "✔ Card created: $CARD_ID"

  if [ "$FIRST" = true ]; then
    FIRST=false
  else
    echo "," >> "$OUTPUT_FILE"
  fi

  echo "  {\"fullName\":\"$FULL_NAME\",\"userId\":\"$USER_ID\",\"accountId\":\"$ACCOUNT_ID\",\"cardId\":\"$CARD_ID\"}" >> "$OUTPUT_FILE"

  sleep 0.2
done

echo "]" >> "$OUTPUT_FILE"

echo "✅ Cards written to $OUTPUT_FILE"