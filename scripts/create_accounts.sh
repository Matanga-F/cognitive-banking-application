#!/bin/bash

BASE_URL="http://localhost:8080/api/cognitive/bank/accounts"
USERS_FILE="users_created.json"
OUTPUT_FILE="accounts_created.json"

ACCOUNT_TYPES=("CHECKING" "SAVINGS" "BROKERAGE")
CURRENCIES=("USD" "EUR" "ZAR")

if [ ! -f "$USERS_FILE" ]; then
  echo "❌ $USERS_FILE not found"
  exit 1
fi

echo "[" > "$OUTPUT_FILE"
COUNT=0

# Read the JSON file line by line
while IFS= read -r line; do
  # Skip empty lines and brackets
  [[ "$line" =~ ^[[:space:]]*[\[\],][[:space:]]*$ ]] && continue

  # Extract userId and fullName using sed
  USER_ID=$(echo "$line" | sed -n 's/.*"userId":"\([^"]*\)".*/\1/p')
  FULL_NAME=$(echo "$line" | sed -n 's/.*"fullName":"\([^"]*\)".*/\1/p')

  # Clean up trailing commas from JSON
  USER_ID=${USER_ID%,}
  FULL_NAME=${FULL_NAME%,}

  if [ -z "$USER_ID" ] || [[ "$USER_ID" == failed* ]]; then
    continue
  fi

  ACCOUNT_TYPE=${ACCOUNT_TYPES[$RANDOM % ${#ACCOUNT_TYPES[@]}]}
  CURRENCY=${CURRENCIES[$RANDOM % ${#CURRENCIES[@]}]}
  BALANCE=$((RANDOM % 5000 + 1000))

  echo "Creating $ACCOUNT_TYPE account for $FULL_NAME"

  RESPONSE=$(curl -s -X POST "$BASE_URL" \
    -H "Content-Type: application/json" \
    -d "{
      \"userId\": \"$USER_ID\",
      \"accountType\": \"$ACCOUNT_TYPE\",
      \"currency\": \"$CURRENCY\",
      \"initialBalance\": $BALANCE,
      \"status\": \"ACTIVE\"
    }")

  ACCOUNT_ID=$(echo "$RESPONSE" | grep -o '"accountId":"[^"]*"' | cut -d'"' -f4)

  if [ -z "$ACCOUNT_ID" ]; then
    ACCOUNT_ID="acc-$(date +%s)$((RANDOM % 1000))"
    echo "⚠️  Using generated account ID for $FULL_NAME"
  else
    echo "✔ Created account $ACCOUNT_ID"
  fi

  if [ $COUNT -gt 0 ]; then
    echo "," >> "$OUTPUT_FILE"
  fi

  echo "  {\"fullName\":\"$FULL_NAME\",\"userId\":\"$USER_ID\",\"accountId\":\"$ACCOUNT_ID\"}" >> "$OUTPUT_FILE"
  COUNT=$((COUNT + 1))

  sleep 0.2
done < <(cat "$USERS_FILE")

echo "]" >> "$OUTPUT_FILE"

echo "✅ Created $COUNT accounts in $OUTPUT_FILE"