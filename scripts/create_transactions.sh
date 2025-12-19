#!/bin/bash

BASE_URL="http://localhost:8080/api/cognitive/bank/transactions"

ACCOUNTS_FILE="accounts_created.json"
OUTPUT_FILE="transactions_created.json"

CURRENCIES=("USD" "ZAR")
DEPOSIT_AMOUNT=1000.00
TRANSFER_AMOUNT=250.00

[ ! -f "$ACCOUNTS_FILE" ] && echo "❌ accounts_created.json not found" && exit 1

echo "[" > "$OUTPUT_FILE"
FIRST=true

# Flatten accounts JSON
ACCOUNTS=$(cat "$ACCOUNTS_FILE" | tr -d '\n' | sed 's/},{/}\n{/g' | sed 's/^\[\|\]$//g')

PREV_ACCOUNT_ID=""

for acc in $ACCOUNTS; do

  ACCOUNT_ID=$(echo "$acc" | sed -n 's/.*"accountId":"\([^"]\+\)".*/\1/p')
  FULL_NAME=$(echo "$acc" | sed -n 's/.*"fullName":"\([^"]\+\)".*/\1/p')

  [ -z "$ACCOUNT_ID" ] && continue

  CURRENCY=${CURRENCIES[$RANDOM % ${#CURRENCIES[@]}]}

  echo "💰 Depositing to $FULL_NAME"

  # -------- DEPOSIT --------
  RESPONSE=$(curl -s -X POST "$BASE_URL" \
    -H "Content-Type: application/json" \
    -d "{
      \"transactionType\": \"DEPOSIT\",
      \"amount\": $DEPOSIT_AMOUNT,
      \"currency\": \"$CURRENCY\",
      \"description\": \"Initial deposit\",
      \"fromAccountId\": \"$ACCOUNT_ID\"
    }")

  TX_ID=$(echo "$RESPONSE" | sed -n 's/.*"transactionId":"\([^"]\+\)".*/\1/p')

  if [ -n "$TX_ID" ]; then
    if [ "$FIRST" = true ]; then FIRST=false; else echo "," >> "$OUTPUT_FILE"; fi
    echo "  {\"type\":\"DEPOSIT\",\"accountId\":\"$ACCOUNT_ID\",\"transactionId\":\"$TX_ID\"}" >> "$OUTPUT_FILE"
  fi

  # -------- TRANSFER --------
  if [ -n "$PREV_ACCOUNT_ID" ]; then
    echo "🔁 Transfer from $PREV_ACCOUNT_ID → $ACCOUNT_ID"

    RESPONSE=$(curl -s -X POST "$BASE_URL" \
      -H "Content-Type: application/json" \
      -d "{
        \"transactionType\": \"TRANSFER_OUT\",
        \"amount\": $TRANSFER_AMOUNT,
        \"currency\": \"$CURRENCY\",
        \"description\": \"Peer transfer\",
        \"fromAccountId\": \"$PREV_ACCOUNT_ID\",
        \"toAccountId\": \"$ACCOUNT_ID\"
      }")

    TX_ID=$(echo "$RESPONSE" | sed -n 's/.*"transactionId":"\([^"]\+\)".*/\1/p')

    if [ -n "$TX_ID" ]; then
      echo "," >> "$OUTPUT_FILE"
      echo "  {\"type\":\"TRANSFER\",\"from\":\"$PREV_ACCOUNT_ID\",\"to\":\"$ACCOUNT_ID\",\"transactionId\":\"$TX_ID\"}" >> "$OUTPUT_FILE"
    fi
  fi

  PREV_ACCOUNT_ID="$ACCOUNT_ID"
  sleep 0.2
done

echo "]" >> "$OUTPUT_FILE"

echo "✅ Transactions saved to $OUTPUT_FILE"
