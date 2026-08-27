#!/usr/bin/env bash
set -euo pipefail

# Script to build and install the Cherokee Forced-Alignment ELAN Plugin
# Usage: ./scripts/build-and-install.sh [/path/to/ELAN.app]

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 1. Run build step
"${SCRIPT_DIR}/build.sh" "$@"

# 2. Run install step
"${SCRIPT_DIR}/install.sh" "$@"
