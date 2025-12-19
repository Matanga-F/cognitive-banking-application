#!/bin/bash

BASE_URL="http://localhost:8080/api/cognitive/bank/users"
PASSWORD="ClientOps#2025"
OUTPUT_FILE="users_created.json"

FIRST_NAMES=("Kwame" "Nia" "Chidi" "Amina" "Tendai" "Zola" "Kofi" "Lerato" "James" "Emma" "Lucas" "Sophia")
LAST_NAMES=("Mensah" "Nkosi" "Okoro" "Diallo" "Smith" "Johnson" "Brown" "Williams")

ROLES=("CUSTOMER" "SUPPORT" "MANAGER")
STATUSES=("ACTIVE")

echo "[" > "$OUTPUT_FILE"

COUNT=1

while [ $COUNT -le 30 ]; do

  FN=${FIRST_NAMES[$RANDOM % ${#FIRST_NAMES[@]}]}
  LN=${LAST_NAMES[$RANDOM % ${#LAST_NAMES[@]}]}

  EMAIL="user${COUNT}@clientservices.com"
  PHONE="+27110000$(printf "%03d" $COUNT)"
  ROLE=${ROLES[$RANDOM % ${#ROLES[@]}]}
  STATUS=${STATUSES[0]}

  RESPONSE=$(curl -s -X POST "$BASE_URL" \
    -H "Content-Type: application/json" \
    -d "{
      \"firstName\": \"$FN\",
      \"lastName\": \"$LN\",
      \"email\": \"$EMAIL\",
      \"password\": \"$PASSWORD\",
      \"phoneNumber\": \"$PHONE\",
      \"role\": \"$ROLE\",
      \"status\": \"$STATUS\"
    }")

  USER_ID=$(echo "$RESPONSE" | sed -n 's/.*"userId":"\([^"]*\)".*/\1/p')
  FULL_NAME=$(echo "$RESPONSE" | sed -n 's/.*"fullName":"\([^"]*\)".*/\1/p')

  echo "✔ Created $FULL_NAME ($USER_ID)"

  if [ $COUNT -lt 30 ]; then
    echo "  {\"userId\":\"$USER_ID\",\"fullName\":\"$FULL_NAME\"}," >> "$OUTPUT_FILE"
  else
    echo "  {\"userId\":\"$USER_ID\",\"fullName\":\"$FULL_NAME\"}" >> "$OUTPUT_FILE"
  fi

  COUNT=$((COUNT + 1))
  sleep 0.2
done

echo "]" >> "$OUTPUT_FILE"

echo "✅ Done. Saved to $OUTPUT_FILE"
