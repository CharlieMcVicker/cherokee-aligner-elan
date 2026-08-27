#!/usr/bin/env bash
set -euo pipefail

# Script to install the Cherokee Forced-Alignment ELAN Plugin (binary + descriptors)
# Usage: ./scripts/install.sh [/path/to/ELAN.app] [/path/to/cherokee-aligner-plugin.jar]

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

ELAN_APP_ARG="${1:-}"
CUSTOM_JAR_ARG="${2:-}"

# 1. Locate ELAN Application Bundle
ELAN_APP="$(find_elan_app "${ELAN_APP_ARG}")"
if [[ ! -d "${ELAN_APP}" ]]; then
    echo "Error: Target ELAN installation must be a valid directory (.app): ${ELAN_APP}" >&2
    exit 1
fi
echo "==> Target ELAN installation: ${ELAN_APP}"

# Detect ELAN major version of target application
TARGET_VERSION="$(detect_elan_version "${ELAN_APP}")"
echo "==> Detected ELAN version for target: ${TARGET_VERSION}"

# 2. Locate Plugin JAR
PLUGIN_JAR="${CUSTOM_JAR_ARG}"
if [[ -z "${PLUGIN_JAR}" ]]; then
    PLUGIN_JAR="$(find_built_jar "${TARGET_VERSION}")"
fi

if [[ -z "${PLUGIN_JAR}" || ! -f "${PLUGIN_JAR}" ]]; then
    echo "Error: Plugin JAR not found for ELAN ${TARGET_VERSION}." >&2
    echo "To build from source and install, run: ./scripts/build.sh --elan ${TARGET_VERSION}" >&2
    echo "Or supply the path to a pre-built JAR: $0 [ELAN.app] [/path/to/plugin.jar]" >&2
    exit 1
fi

echo "==> Using Plugin JAR: ${PLUGIN_JAR}"

# 3. Verify descriptor files exist
if [[ ! -f "${CMDI_FILE1}" || ! -f "${CMDI_FILE2}" ]]; then
    echo "Error: Required CMDI descriptor files not found in ${ELAN_PLUGIN_DIR}/src/main/resources/" >&2
    exit 1
fi

# 4. Determine and verify target extensions directory
EXTENSIONS_DIR="${ELAN_APP}/Contents/app/extensions/cherokee-aligner-ext"
echo "==> Target extensions directory: ${EXTENSIONS_DIR}"

check_writable_dir "${EXTENSIONS_DIR}" "${ELAN_APP}"

# 5. Copy files
echo "==> Copying plugin JAR and recognizer descriptors..."
cp -f "${PLUGIN_JAR}" "${CMDI_FILE1}" "${CMDI_FILE2}" "${EXTENSIONS_DIR}/"

echo "==> Successfully installed Cherokee Forced-Alignment plugin to ${ELAN_APP}!"
echo "==> Restart ELAN to load the plugin."
