#!/usr/bin/env bash
set -euo pipefail

# Script to install the Cherokee Forced-Alignment ELAN Plugin (binary + descriptors)
# Usage: ./scripts/install.sh [/path/to/ELAN.app] [/path/to/cherokee-aligner-plugin.jar]

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

ELAN_APP_ARG="${1:-}"
CUSTOM_ARG="${2:-}"
ZIP_ARG=""

if [[ "${ELAN_APP_ARG}" =~ \.zip$ && -f "${ELAN_APP_ARG}" ]]; then
    ZIP_ARG="${ELAN_APP_ARG}"
    ELAN_APP_ARG=""
elif [[ "${CUSTOM_ARG}" =~ \.zip$ && -f "${CUSTOM_ARG}" ]]; then
    ZIP_ARG="${CUSTOM_ARG}"
    CUSTOM_ARG=""
fi

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

# Determine and verify target extensions directory
EXTENSIONS_DIR="${ELAN_APP}/Contents/app/extensions/cherokee-aligner-ext"
echo "==> Target extensions directory: ${EXTENSIONS_DIR}"

check_writable_dir "${EXTENSIONS_DIR}" "${ELAN_APP}"

# If a zip archive was provided, unzip directly into extensions folder
if [[ -n "${ZIP_ARG}" && -f "${ZIP_ARG}" ]]; then
    echo "==> Installing from extension archive: ${ZIP_ARG}..."
    unzip -o -q "${ZIP_ARG}" -d "${EXTENSIONS_DIR}"
    echo "==> Successfully installed Cherokee Forced-Alignment plugin to ${ELAN_APP}!"
    echo "==> Restart ELAN to load the plugin."
    exit 0
fi

# 2. Locate Plugin JAR
PLUGIN_JAR="${CUSTOM_ARG}"
if [[ -z "${PLUGIN_JAR}" ]]; then
    PLUGIN_JAR="$(find_built_jar "${TARGET_VERSION}")"
fi

if [[ -z "${PLUGIN_JAR}" || ! -f "${PLUGIN_JAR}" ]]; then
    # Check if a built zip exists in dist/
    if [[ -f "${DIST_DIR}/cherokee-aligner-elan-${TARGET_VERSION}.zip" ]]; then
        echo "==> Installing from built zip archive in dist/..."
        unzip -o -q "${DIST_DIR}/cherokee-aligner-elan-${TARGET_VERSION}.zip" -d "${EXTENSIONS_DIR}"
        echo "==> Successfully installed Cherokee Forced-Alignment plugin to ${ELAN_APP}!"
        echo "==> Restart ELAN to load the plugin."
        exit 0
    fi
    echo "Error: Plugin JAR or ZIP not found for ELAN ${TARGET_VERSION}." >&2
    echo "To build from source and install, run: ./scripts/build.sh --elan ${TARGET_VERSION}" >&2
    echo "Or supply the path to a pre-built JAR or ZIP: $0 [ELAN.app] [/path/to/archive.zip or plugin.jar]" >&2
    exit 1
fi

echo "==> Using Plugin JAR: ${PLUGIN_JAR}"

# 3. Verify descriptor files exist
if [[ ! -f "${CMDI_FILE1}" || ! -f "${CMDI_FILE2}" ]]; then
    echo "Error: Required CMDI descriptor files not found in ${ELAN_PLUGIN_DIR}/src/main/resources/" >&2
    exit 1
fi

# 4. Copy files
echo "==> Copying plugin JAR and recognizer descriptors..."
cp -f "${PLUGIN_JAR}" "${CMDI_FILE1}" "${CMDI_FILE2}" "${EXTENSIONS_DIR}/"

echo "==> Successfully installed Cherokee Forced-Alignment plugin to ${ELAN_APP}!"
echo "==> Restart ELAN to load the plugin."
