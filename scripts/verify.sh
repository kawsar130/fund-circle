#!/usr/bin/env bash
# ==============================================================================
# Fund Circle — Local Fast-Verification & Quality Gate Script
# ==============================================================================
set -euo pipefail

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}======================================================${NC}"
echo -e "${BLUE}  Fund Circle Pre-Flight Verification & Quality Gates ${NC}"
echo -e "${BLUE}======================================================${NC}\n"

# 1. Invariant 1: Whole-Integer Money (No Float or Double in financial entities/parameters/functions)
echo -e "🔍 [1/3] Checking Architectural Invariant 1 (64-bit Long monetary model)..."
if [ -d "app/src/main" ]; then
    FORBIDDEN_MONEY=$(grep -rnE '\b[a-zA-Z0-9_]*(amount|balance|deposit|expense|target|total)[a-zA-Z0-9_]*\s*:\s*(Double|Float)' app/src/main/ || true)
    if [ -n "$FORBIDDEN_MONEY" ]; then
        echo -e "${RED}❌ FAILED: Invariant 1 Violation! Found forbidden Double or Float monetary declarations:${NC}"
        echo "$FORBIDDEN_MONEY"
        exit 1
    fi
    echo -e "${GREEN}✓ Monetary invariant satisfied (Zero Double/Float in financial fields).${NC}"
else
    echo -e "${YELLOW}ℹ app/src/main directory not present yet. Skipping source scan.${NC}"
fi

# 2. Invariant 4: Zero Hard Deletes (Verify no delete() on transaction collections/batches/tx)
echo -e "\n🔍 [2/3] Checking Architectural Invariant 4 (Immutable Ledger / No Hard Deletes)..."
if [ -d "app/src/main" ]; then
    FORBIDDEN_DELETE=$(grep -rnE '(\.delete\(|(batch|tx|transaction)\.delete\()' app/src/main/ || true)
    if [ -n "$FORBIDDEN_DELETE" ]; then
        echo -e "${RED}❌ FAILED: Invariant 4 Violation! Found physical document or batch delete() on transactions:${NC}"
        echo "$FORBIDDEN_DELETE"
        echo -e "${YELLOW}Transactions must use the Transaction Reversal Protocol (isVoid = true, FieldValue.increment(-amount)).${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ Ledger immutability satisfied.${NC}"
else
    echo -e "${YELLOW}ℹ app/src/main directory not present yet. Skipping source scan.${NC}"
fi

# 3. Gradle Quality Gates (Unit Tests & Lint)
echo -e "\n🔍 [3/3] Running Gradle Quality Gates..."
if [ -f "./gradlew" ]; then
    chmod +x ./gradlew
    echo -e "Running unit test suite..."
    ./gradlew testDebugUnitTest --no-daemon
    
    echo -e "Running Android lint..."
    ./gradlew lintDebug --no-daemon
    
    echo -e "${GREEN}✓ All Gradle unit tests and lint checks passed.${NC}"
else
    echo -e "${YELLOW}ℹ gradlew not found yet.${NC}"
fi

echo -e "\n${GREEN}======================================================${NC}"
echo -e "${GREEN}  ✓ Pre-Flight Verification Passed Successfully!     ${NC}"
echo -e "${GREEN}======================================================${NC}"
