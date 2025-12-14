#!/bin/bash
# =============================================================================
# fetch_update.sh - AeroJudge Update Fetcher
# =============================================================================
# This script is installed on the device and fetches/executes the latest
# update script from GitHub. This allows the main update logic to be
# updated remotely without needing to modify files on individual devices.
#
# Usage:
#   /home/judge/fetch_update.sh
#
# Exit Codes (passed through from judge_update.sh):
#   0 = No update needed (already running latest version)
#   1 = Error occurred during update
#   2 = Update successfully applied
# =============================================================================

UPDATE_URL="https://raw.githubusercontent.com/IMAC-ORG/imac-judge-app/main/scripts/judge_update.sh"

curl -sfS "$UPDATE_URL" | bash
exit $?
