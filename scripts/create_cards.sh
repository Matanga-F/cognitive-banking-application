#!/bin/bash

BASE_URL="http://localhost:8080/api/cognitive/bank/cards"
ACCOUNTS_FILE="accounts_created.json"
OUTPUT_FILE="cards_created.json"

CARD_TYPES=("DEBIT" "CREDIT")
DAILY_LIMIT=3000.00
CREDIT_LIMIT=5000.00

# Check files exist
[ ! -f "$ACCOUNTS_FILE" ] && echo "❌ $ACCOUNTS_FILE not found" && exit 1

echo "[" > "$OUTPUT_FILE"
COUNT=0
LINE_NUM=0

# Read accounts file line by line
while IFS= read -r line; do
  LINE_NUM=$((LINE_NUM + 1))

  # Skip empty lines and brackets
  [[ "$line" =~ ^[[:space:]]*[\[\],][[:space:]]*$ ]] && continue

  # Extract data from the line
  USER_ID=$(echo "$line" | sed -n 's/.*"userId":"\([^"]*\)".*/\1/p')
  ACCOUNT_ID=$(echo "$line" | sed -n 's/.*"accountId":"\([^"]*\)".*/\1/p')
  FULL_NAME=$(echo "$line" | sed -n 's/.*"fullName":"\([^"]*\)".*/\1/p')

  # Clean any trailing commas
  USER_ID=${USER_ID%,}
  ACCOUNT_ID=${ACCOUNT_ID%,}
  FULL_NAME=${FULL_NAME%,}

  # Skip if any field is missing or invalid
  if [ -z "$USER_ID" ] || [ -z "$ACCOUNT_ID" ] || [ -z "$FULL_NAME" ] || \
     [[ "$ACCOUNT_ID" == failed* ]] || [[ "$ACCOUNT_ID" == error* ]]; then
    continue
  fi

  # Random card type
  CARD_TYPE=${CARD_TYPES[$RANDOM % ${#CARD_TYPES[@]}]}

  echo "Creating $CARD_TYPE card for $FULL_NAME"

  # Make API call
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

  # Extract card ID - try multiple patterns
  CARD_ID=$(echo "$RESPONSE" | grep -o '"cardId":"[^"]*"' | cut -d'"' -f4)

  if [ -z "$CARD_ID" ]; then
    CARD_ID=$(echo "$RESPONSE" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
  fi

  if [ -z "$CARD_ID" ]; then
    # If curl failed or no response
    if [[ -z "$RESPONSE" ]] || [[ "$RESPONSE" == *"curl:"* ]]; then
      echo "❌ Connection failed for $FULL_NAME"
      CARD_ID="failed-card-$((RANDOM % 1000))"
    else
      echo "❌ Card creation failed"
      CARD_ID="error-$LINE_NUM"
    fi
  else
    echo "✔ Card created: $CARD_ID"
  fi

  # Write to output file
  if [ $COUNT -gt 0 ]; then
    echo "," >> "$OUTPUT_FILE"
  fi

  echo "  {\"fullName\":\"$FULL_NAME\",\"userId\":\"$USER_ID\",\"accountId\":\"$ACCOUNT_ID\",\"cardId\":\"$CARD_ID\",\"cardType\":\"$CARD_TYPE\"}" >> "$OUTPUT_FILE"
  COUNT=$((COUNT + 1))

  sleep 0.2

done < "$ACCOUNTS_FILE"

echo "]" >> "$OUTPUT_FILE"

echo "✅ Created $COUNT cards in $OUTPUT_FILE"