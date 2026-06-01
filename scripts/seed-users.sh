#!/bin/bash

# Configuration
API_URL="http://127.0.0.1:56460/api/cognitive/bank/users"
OUTPUT_FILE="created_users.json"
LOG_FILE="user_creation.log"

# Data arrays
FIRST_NAMES=("Ethan" "Sophia" "Liam" "Olivia" "Noah" "Ava" "Mason" "Isabella" "Lucas" "Mia" "James" "Charlotte" "Benjamin" "Amelia" "Elijah" "Harper" "Alexander" "Evelyn" "Daniel" "Abigail")
LAST_NAMES=("Brooks" "Johnson" "Smith" "Williams" "Brown" "Jones" "Garcia" "Miller" "Davis" "Rodriguez" "Martinez" "Hernandez" "Lopez" "Gonzalez" "Wilson" "Anderson" "Thomas" "Taylor" "Moore" "Jackson")
ROLES=("CUSTOMER" "ADMIN" "SUPPORT" "MANAGER")
STATUSES=("ACTIVE" "INACTIVE" "SUSPENDED" "PENDING_VERIFICATION")

# Clear or create log file
echo "User Creation Log - $(date)" > "$LOG_FILE"

# Start JSON array
echo "[" > "$OUTPUT_FILE"

# Get the total number of users to create
TOTAL_USERS=${#FIRST_NAMES[@]}
SUCCESS_COUNT=0
FAIL_COUNT=0

echo "Creating $TOTAL_USERS users..."
echo "API URL: $API_URL"
echo "Output file: $OUTPUT_FILE"
echo "Log file: $LOG_FILE"
echo ""

# First, test the API connection
echo "Testing API connection..."
TEST_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X GET "${API_URL%/*}" 2>/dev/null || echo "000")
if [ "$TEST_RESPONSE" != "200" ]; then
    echo "⚠️  WARNING: API might not be available (HTTP $TEST_RESPONSE)"
    echo "Please ensure the server is running at: $API_URL"
    read -p "Continue anyway? (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
else
    echo "✓ API connection successful"
fi
echo ""

# Function to generate random date
generate_random_date() {
    local year=$((RANDOM % 24 + 2000))  # 2000-2024
    local month=$(printf "%02d" $((RANDOM % 12 + 1)))
    local day=$(printf "%02d" $((RANDOM % 28 + 1)))
    echo "${year}-${month}-${day}"
}

# Create users
FIRST_ENTRY=true
for ((i=0; i<TOTAL_USERS; i++)); do
    # Generate user data with unique email
    FIRST_NAME="${FIRST_NAMES[$i]}"
    LAST_NAME="${LAST_NAMES[$i]}"
    # Add index to email to ensure uniqueness
    EMAIL="${FIRST_NAME,,}.${LAST_NAME,,}${i}@example.com"
    PASSWORD="Password123!"
    PHONE="+1$(printf %03d $((RANDOM % 900 + 100)))555$(printf %04d $((RANDOM % 10000)))"
    DATE_OF_BIRTH=$(generate_random_date)
    ROLE="${ROLES[$((RANDOM % ${#ROLES[@]}))]}"
    STATUS="${STATUSES[$((RANDOM % ${#STATUSES[@]}))]}"

    # Create JSON payload
    JSON_PAYLOAD=$(cat <<EOF
{
    "email": "$EMAIL",
    "password": "$PASSWORD",
    "firstName": "$FIRST_NAME",
    "lastName": "$LAST_NAME",
    "phone": "$PHONE",
    "dateOfBirth": "$DATE_OF_BIRTH",
    "role": "$ROLE",
    "status": "$STATUS"
}
EOF
)

    echo "Creating user $((i+1))/$TOTAL_USERS: $FIRST_NAME $LAST_NAME ($EMAIL)"
    echo "Role: $ROLE, Status: $STATUS"

    # Log request
    echo "=== Request $((i+1)) ===" >> "$LOG_FILE"
    echo "Endpoint: POST $API_URL" >> "$LOG_FILE"
    echo "Payload: $JSON_PAYLOAD" >> "$LOG_FILE"

    # Make API call with timeout and full response capture
    START_TIME=$(date +%s)
    RESPONSE=$(curl -s -X POST "$API_URL" \
        -H "Content-Type: application/json" \
        -d "$JSON_PAYLOAD" \
        -w "\nHTTP_STATUS:%{http_code}\nTIME:%{time_total}" \
        --max-time 10 \
        --connect-timeout 5)
    END_TIME=$(date +%s)
    DURATION=$((END_TIME - START_TIME))

    # Extract response components
    HTTP_STATUS=$(echo "$RESPONSE" | grep 'HTTP_STATUS:' | cut -d':' -f2)
    RESPONSE_TIME=$(echo "$RESPONSE" | grep 'TIME:' | cut -d':' -f2)
    RESPONSE_BODY=$(echo "$RESPONSE" | sed '/HTTP_STATUS:/d' | sed '/TIME:/d')

    # Log response
    echo "Response time: ${RESPONSE_TIME}s (total: ${DURATION}s)" >> "$LOG_FILE"
    echo "HTTP Status: $HTTP_STATUS" >> "$LOG_FILE"
    echo "Response body: $RESPONSE_BODY" >> "$LOG_FILE"
    echo "" >> "$LOG_FILE"

    # Process response
    if [[ "$HTTP_STATUS" =~ ^2[0-9][0-9]$ ]]; then
        echo "  ✓ Success (HTTP $HTTP_STATUS in ${RESPONSE_TIME}s)"
        ((SUCCESS_COUNT++))

        # Add to output file with comma if not first entry
        if [ "$FIRST_ENTRY" = false ]; then
            echo "," >> "$OUTPUT_FILE"
        fi

        # Save response to output file
        echo "$RESPONSE_BODY" >> "$OUTPUT_FILE"
        FIRST_ENTRY=false

    elif [[ -z "$HTTP_STATUS" ]]; then
        echo "  ✗ Failed - No response from server (connection timeout?)"
        echo "  Check if server is still running at: ${API_URL%/*}"
        ((FAIL_COUNT++))

        # Ask if user wants to continue
        if [ $i -gt 0 ]; then
            read -p "  Continue? (y/n/skip to next): " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Nn]$ ]]; then
                break
            elif [[ $REPLY =~ ^[Ss]$ ]]; then
                echo "  Skipping user $((i+1))..."
                continue
            fi
        fi
    else
        echo "  ✗ Failed (HTTP $HTTP_STATUS in ${RESPONSE_TIME}s)"
        echo "  Response: $RESPONSE_BODY"
        ((FAIL_COUNT++))
    fi

    # Increasing delay to avoid rate limiting
    DELAY=$(( (i / 5) + 1 ))
    if [ $DELAY -gt 3 ]; then
        DELAY=3
    fi
    echo "  Waiting ${DELAY}s before next request..."
    sleep $DELAY

    echo ""
done

# Close JSON array
echo "]" >> "$OUTPUT_FILE"

# Summary
echo ""
echo "========================================"
echo "SUMMARY"
echo "========================================"
echo "Total attempts: $TOTAL_USERS"
echo "Successful: $SUCCESS_COUNT"
echo "Failed: $FAIL_COUNT"
echo "Output saved to: $OUTPUT_FILE"
echo "Detailed log: $LOG_FILE"
echo ""

# Check if server is still responding
echo "Verifying API is still accessible..."
FINAL_TEST=$(curl -s -o /dev/null -w "%{http_code}" -X GET "${API_URL%/*}" --max-time 5 2>/dev/null || echo "000")
if [ "$FINAL_TEST" != "200" ]; then
    echo "⚠️  API appears to be unavailable now (HTTP $FINAL_TEST)"
    echo "   The server might have crashed during user creation."
else
    echo "✓ API is still responding"
fi

# Display first few lines of log if there were failures
if [ $FAIL_COUNT -gt 0 ]; then
    echo ""
    echo "Last 3 log entries:"
    tail -20 "$LOG_FILE"
fi