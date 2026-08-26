#!/usr/bin/env bash
set -euo pipefail

# Script to build and install/update the Cherokee Forced-Alignment ELAN Plugin

REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ELAN_PLUGIN_DIR="${REPO_DIR}/elan-plugin"
TARGET_JAR="${ELAN_PLUGIN_DIR}/target/cherokee-aligner-plugin-1.0.0-SNAPSHOT.jar"
CMDI_FILE1="${ELAN_PLUGIN_DIR}/src/main/resources/cherokee-aligner.cmdi"
CMDI_FILE2="${ELAN_PLUGIN_DIR}/src/main/resources/recognizer.cmdi"

# 1. Build the plugin jar
echo "==> Building ELAN Plugin..."
(cd "${ELAN_PLUGIN_DIR}" && mvn clean package -DskipTests)

if [[ ! -f "${TARGET_JAR}" ]]; then
    echo "Error: Built JAR not found at ${TARGET_JAR}" >&2
    exit 1
fi

# 2. Locate ELAN Application Bundle
ELAN_APP="${1:-}"
if [[ -z "${ELAN_APP}" ]]; then
    # Look for ELAN installations in /Applications
    ELAN_APP="$(find /Applications -maxdepth 1 -iname "ELAN*.app" 2>/dev/null | sort -V | tail -n 1 || true)"
fi

if [[ -z "${ELAN_APP}" || ! -d "${ELAN_APP}" ]]; then
    echo "Error: Could not automatically locate ELAN in /Applications." >&2
    echo "Usage: $0 [/path/to/ELAN_version.app]" >&2
    exit 1
fi

EXTENSIONS_DIR="${ELAN_APP}/Contents/app/extensions/cherokee-aligner-ext"

echo "==> Installing plugin to: ${EXTENSIONS_DIR}"
sudo mkdir -p "${EXTENSIONS_DIR}"
sudo cp -f "${TARGET_JAR}" "${CMDI_FILE1}" "${CMDI_FILE2}" "${EXTENSIONS_DIR}/"

echo "==> Successfully installed Cherokee Forced-Alignment plugin to ${ELAN_APP}!"
echo "==> Please restart ELAN to load the updated plugin."
