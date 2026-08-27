#!/usr/bin/env bash
set -euo pipefail

# Script to build and install/update the Cherokee Forced-Alignment ELAN Plugin

REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ELAN_PLUGIN_DIR="${REPO_DIR}/elan-plugin"
TARGET_JAR="${ELAN_PLUGIN_DIR}/target-out/cherokee-aligner-plugin-1.0.0-SNAPSHOT.jar"
if [[ ! -f "${TARGET_JAR}" && -f "${ELAN_PLUGIN_DIR}/target/cherokee-aligner-plugin-1.0.0-SNAPSHOT.jar" ]]; then
    TARGET_JAR="${ELAN_PLUGIN_DIR}/target/cherokee-aligner-plugin-1.0.0-SNAPSHOT.jar"
fi
CMDI_FILE1="${ELAN_PLUGIN_DIR}/src/main/resources/cherokee-aligner.cmdi"
CMDI_FILE2="${ELAN_PLUGIN_DIR}/src/main/resources/recognizer.cmdi"

# 1. Locate ELAN Application Bundle
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

echo "==> Using ELAN installation: ${ELAN_APP}"

# 2. Locate and link elan.jar into elan-plugin/lib/elan.jar
mkdir -p "${ELAN_PLUGIN_DIR}/lib"
FOUND_ELAN_JAR=""

if [[ -f "${ELAN_APP}/Contents/app/elan.jar" ]]; then
    FOUND_ELAN_JAR="${ELAN_APP}/Contents/app/elan.jar"
elif [[ -f "${ELAN_APP}/Contents/Java/elan.jar" ]]; then
    FOUND_ELAN_JAR="${ELAN_APP}/Contents/Java/elan.jar"
elif [[ -f "${ELAN_APP}/Contents/Resources/app/elan.jar" ]]; then
    FOUND_ELAN_JAR="${ELAN_APP}/Contents/Resources/app/elan.jar"
else
    FOUND_ELAN_JAR="$(find "${ELAN_APP}" -iname "elan*.jar" ! -iname "*plugin*" ! -iname "*ext*" 2>/dev/null | head -n 1 || true)"
fi

if [[ -n "${FOUND_ELAN_JAR}" && -f "${FOUND_ELAN_JAR}" ]]; then
    echo "==> Found ELAN jar: ${FOUND_ELAN_JAR}"
    echo "==> Linking to ${ELAN_PLUGIN_DIR}/lib/elan.jar..."
    ln -sf "${FOUND_ELAN_JAR}" "${ELAN_PLUGIN_DIR}/lib/elan.jar"
elif [[ ! -f "${ELAN_PLUGIN_DIR}/lib/elan.jar" ]]; then
    echo "Warning: Could not automatically find elan.jar inside ${ELAN_APP}." >&2
fi

# 3. Build the plugin jar
echo "==> Building ELAN Plugin..."
(cd "${ELAN_PLUGIN_DIR}" && mvn clean package -DskipTests)

if [[ ! -f "${TARGET_JAR}" ]]; then
    echo "Error: Built JAR not found at ${TARGET_JAR}" >&2
    exit 1
fi

EXTENSIONS_DIR="${ELAN_APP}/Contents/app/extensions/cherokee-aligner-ext"

echo "==> Installing plugin to: ${EXTENSIONS_DIR}"
mkdir -p "${EXTENSIONS_DIR}"
cp -f "${TARGET_JAR}" "${CMDI_FILE1}" "${CMDI_FILE2}" "${EXTENSIONS_DIR}/"

echo "==> Successfully installed Cherokee Forced-Alignment plugin to ${ELAN_APP}!"
echo "==> Please restart ELAN to load the updated plugin."
