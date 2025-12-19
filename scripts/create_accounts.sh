#!/bin/bash

BASE_URL="http://localhost:8080/api/cognitive/bank/accounts"
USERS_FILE="users_created.json"
OUTPUT_FILE="accounts_created.json"

ACCOUNT_TYPES=("CHECKING" "SAVINGS" "BROKERAGE")
CURRENCIES=("USD" "EUR" "ZAR")

if [ ! -f "$USERS_FILE" ]; then
  echo "❌ users_created.json not found"
  exit 1
fi

echo "[" > "$OUTPUT_FILE"
FIRST=true

# Flatten JSON into individual objects
USERS=$(cat "$USERS_FILE" | tr -d '\n' | sed 's/},{/}\n{/g' | sed 's/^\[\|\]$//g')

for user in $USERS; do

  USER_ID=$(echo "$user" | sed -n 's/.*"userId":"\([^"]\+\)".*/\1/p')
  FULL_NAME=$(echo "$user" | sed -n 's/.*"fullName":"\([^"]\+\)".*/\1/p')

  [ -z "$USER_ID" ] && continue

  ACCOUNT_TYPE=${ACCOUNT_TYPES[$RANDOM % ${#ACCOUNT_TYPES[@]}]}
  CURRENCY=${CURRENCIES[$RANDOM % ${#CURRENCIES[@]}]}
  BALANCE=$((RANDOM % 5000 + 1000))

  echo "Creating account for $FULL_NAME"

  RESPONSE=$(curl -s -X POST "$BASE_URL" \
    -H "Content-Type: application/json" \
    -d "{
      \"userId\": \"$USER_ID\",
      \"accountType\": \"$ACCOUNT_TYPE\",
      \"currency\": \"$CURRENCY\",
      \"initialBalance\": $BALANCE,
      \"status\": \"ACTIVE\"
    }")

  ACCOUNT_ID=$(echo "$RESPONSE" | sed -n 's/.*"accountId":"\([^"]\+\)".*/\1/p')

  if [ -z "$ACCOUNT_ID" ]; then
    echo "❌ Failed for $FULL_NAME"
    echo "Response: $RESPONSE"
    continue
  fi

  echo "✔ Created account $ACCOUNT_ID"

  if [ "$FIRST" = true ]; then
    FIRST=false
  else
    echo "," >> "$OUTPUT_FILE"
  fi

  echo "  {\"fullName\":\"$FULL_NAME\",\"userId\":\"$USER_ID\",\"accountId\":\"$ACCOUNT_ID\"}" >> "$OUTPUT_FILE"

  sleep 0.2
done

echo "]" >> "$OUTPUT_FILE"

echo "✅ Accounts written to $OUTPUT_FILE"
