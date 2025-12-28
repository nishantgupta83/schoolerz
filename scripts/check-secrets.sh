#!/bin/bash
# Pre-push hook to prevent accidental secret commits
# Run: ./scripts/check-secrets.sh before pushing

set -e

SECRETS_FOUND=0
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

check_file() {
    if git ls-files --cached 2>/dev/null | grep -q "$1"; then
        echo -e "${RED}ERROR: $1 is staged for commit!${NC}"
        SECRETS_FOUND=1
    fi
}

echo "Checking for secrets..."

check_file "GoogleService-Info.plist"
check_file "google-services.json"
check_file ".jks"
check_file ".keystore"
check_file ".p12"
check_file ".cer"
check_file ".mobileprovision"

if [ $SECRETS_FOUND -eq 1 ]; then
    echo ""
    echo -e "${RED}Secrets detected! Remove them before pushing.${NC}"
    echo "Run: git reset HEAD <file> to unstage"
    exit 1
fi

echo -e "${GREEN}No secrets found. Safe to push.${NC}"
exit 0
