#!/usr/bin/env bash
set -euo pipefail

# Shared utility functions for Cherokee ELAN plugin scripts

REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ELAN_PLUGIN_DIR="${REPO_DIR}/elan-plugin"
CMDI_FILE1="${ELAN_PLUGIN_DIR}/src/main/resources/cherokee-aligner.cmdi"
CMDI_FILE2="${ELAN_PLUGIN_DIR}/src/main/resources/recognizer.cmdi"

find_elan_app() {
    local requested_app="${1:-}"
    if [[ -n "${requested_app}" ]]; then
        if [[ -d "${requested_app}" ]]; then
            echo "${requested_app}"
            return 0
        else
            echo "Error: Specified ELAN path does not exist: ${requested_app}" >&2
            return 1
        fi
    fi

    local found_app
    found_app="$(find /Applications -maxdepth 1 -iname "ELAN*.app" 2>/dev/null | sort -V | tail -n 1 || true)"
    if [[ -z "${found_app}" || ! -d "${found_app}" ]]; then
        echo "Error: Could not automatically locate ELAN in /Applications." >&2
        echo "Usage: $0 [/path/to/ELAN_version.app]" >&2
        return 1
    fi
    echo "${found_app}"
}

find_built_jar() {
    local target_jar=""
    if [[ -f "${ELAN_PLUGIN_DIR}/target/cherokee-aligner-plugin-1.0.0-SNAPSHOT.jar" ]]; then
        target_jar="${ELAN_PLUGIN_DIR}/target/cherokee-aligner-plugin-1.0.0-SNAPSHOT.jar"
    elif [[ -f "${ELAN_PLUGIN_DIR}/target-out/cherokee-aligner-plugin-1.0.0-SNAPSHOT.jar" ]]; then
        target_jar="${ELAN_PLUGIN_DIR}/target-out/cherokee-aligner-plugin-1.0.0-SNAPSHOT.jar"
    fi
    echo "${target_jar}"
}

link_elan_jar() {
    local elan_app="$1"
    mkdir -p "${ELAN_PLUGIN_DIR}/lib"
    local found_jar=""

    if [[ -f "${elan_app}/Contents/app/elan.jar" ]]; then
        found_jar="${elan_app}/Contents/app/elan.jar"
    elif [[ -f "${elan_app}/Contents/Java/elan.jar" ]]; then
        found_jar="${elan_app}/Contents/Java/elan.jar"
    elif [[ -f "${elan_app}/Contents/Resources/app/elan.jar" ]]; then
        found_jar="${elan_app}/Contents/Resources/app/elan.jar"
    else
        found_jar="$(find "${elan_app}" -iname "elan*.jar" ! -iname "*plugin*" ! -iname "*ext*" 2>/dev/null | head -n 1 || true)"
    fi

    if [[ -n "${found_jar}" && -f "${found_jar}" ]]; then
        echo "==> Found ELAN jar: ${found_jar}"
        echo "==> Linking to ${ELAN_PLUGIN_DIR}/lib/elan.jar..."
        ln -sf "${found_jar}" "${ELAN_PLUGIN_DIR}/lib/elan.jar"
    elif [[ ! -f "${ELAN_PLUGIN_DIR}/lib/elan.jar" ]]; then
        echo "Warning: Could not automatically find elan.jar inside ${elan_app}." >&2
    fi
}

check_writable_dir() {
    local target_dir="$1"
    local elan_app="$2"

    local can_write=1
    if mkdir -p "${target_dir}" 2>/dev/null; then
        local test_file="${target_dir}/.write_test_$$"
        if touch "${test_file}" 2>/dev/null; then
            rm -f "${test_file}"
            can_write=0
        fi
    fi

    if [[ "${can_write}" -ne 0 ]]; then
        echo "Error: Cannot write to extensions directory: ${target_dir}" >&2
        echo "Reason: macOS App Management or file permissions restricted write access to ${elan_app}." >&2
        echo "Troubleshooting:" >&2
        echo "  1. If permissions or ownership are mismatched, run:" >&2
        echo "       sudo chown -R \$(whoami) \"${elan_app}\"" >&2
        echo "  2. If running on macOS with App Management protection, ensure your Terminal has App Management permissions" >&2
        echo "     in System Settings > Privacy & Security > App Management (or run copy with sudo if necessary)." >&2
        return 1
    fi
}
