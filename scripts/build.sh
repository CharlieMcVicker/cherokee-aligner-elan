#!/usr/bin/env bash
set -euo pipefail

# Script to build the Cherokee Forced-Alignment ELAN Plugin JAR
# Usage: ./scripts/build.sh [--elan 6|7|all] [/path/to/ELAN.app]

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

BUILD_MODE="default"
TARGET_APP_OR_VER=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        --all)
            BUILD_MODE="all"
            shift
            ;;
        --elan)
            if [[ $# -lt 2 ]]; then
                echo "Error: --elan requires a version argument ('6', '7', or 'all')" >&2
                exit 1
            fi
            if [[ "$2" == "all" ]]; then
                BUILD_MODE="all"
            elif [[ "$2" =~ ^6 ]]; then
                BUILD_MODE="6"
            elif [[ "$2" =~ ^7 ]]; then
                BUILD_MODE="7"
            else
                echo "Error: Unsupported ELAN version '$2'. Expected 6, 7, or all." >&2
                exit 1
            fi
            shift 2
            ;;
        --elan=*)
            val="${1#*=}"
            if [[ "${val}" == "all" ]]; then
                BUILD_MODE="all"
            elif [[ "${val}" =~ ^6 ]]; then
                BUILD_MODE="6"
            elif [[ "${val}" =~ ^7 ]]; then
                BUILD_MODE="7"
            else
                echo "Error: Unsupported ELAN version '${val}'. Expected 6, 7, or all." >&2
                exit 1
            fi
            shift
            ;;
        -*)
            echo "Error: Unknown option $1" >&2
            echo "Usage: $0 [--elan 6|7|all] [--all] [/path/to/ELAN.app]" >&2
            exit 1
            ;;
        *)
            TARGET_APP_OR_VER="$1"
            shift
            ;;
    esac
done

build_single_elan() {
    local target_spec="$1" # "6", "7", or path to ELAN.app
    local detected_ver
    detected_ver="$(detect_elan_version "${target_spec}")"
    if [[ "${detected_ver}" != "6" && "${detected_ver}" != "7" ]]; then
        detected_ver="7"
    fi

    echo "==> Preparing ELAN dependency for version ${detected_ver} (${target_spec})..."
    link_elan_jar "${target_spec}"

    echo "==> Building ELAN Plugin with Maven (ELAN ${detected_ver})..."
    (cd "${ELAN_PLUGIN_DIR}" && mvn clean package -DskipTests)

    local raw_jar="${ELAN_PLUGIN_DIR}/target-out/cherokee-aligner-plugin-1.0.0-SNAPSHOT.jar"
    if [[ ! -f "${raw_jar}" ]]; then
        raw_jar="${ELAN_PLUGIN_DIR}/target/cherokee-aligner-plugin-1.0.0-SNAPSHOT.jar"
    fi

    if [[ ! -f "${raw_jar}" ]]; then
        echo "Error: Maven build succeeded but output jar not found at ${raw_jar}" >&2
        return 1
    fi

    mkdir -p "${DIST_DIR}" "${ELAN_PLUGIN_DIR}/target-out"

    local versioned_jar_name="cherokee-aligner-plugin-elan${detected_ver}-1.0.0-SNAPSHOT.jar"
    cp -f "${raw_jar}" "${DIST_DIR}/${versioned_jar_name}"
    cp -f "${raw_jar}" "${DIST_DIR}/cherokee-aligner-plugin-1.0.0-SNAPSHOT.jar"
    cp -f "${raw_jar}" "${ELAN_PLUGIN_DIR}/target-out/${versioned_jar_name}"

    echo "==> Build complete for ELAN ${detected_ver}:"
    echo "    - ${DIST_DIR}/${versioned_jar_name}"
    echo "    - ${ELAN_PLUGIN_DIR}/target-out/${versioned_jar_name}"
}

if [[ "${BUILD_MODE}" == "all" ]]; then
    echo "==> Building for all supported ELAN versions (ELAN 6 and ELAN 7)..."
    build_single_elan "6"
    build_single_elan "7"
    # Ensure both versioned jars exist in target-out as well
    if [[ -f "${DIST_DIR}/cherokee-aligner-plugin-elan6-1.0.0-SNAPSHOT.jar" ]]; then
        cp -f "${DIST_DIR}/cherokee-aligner-plugin-elan6-1.0.0-SNAPSHOT.jar" "${ELAN_PLUGIN_DIR}/target-out/"
    fi
    echo "==> All builds completed successfully."
elif [[ "${BUILD_MODE}" == "6" ]]; then
    build_single_elan "6"
elif [[ "${BUILD_MODE}" == "7" ]]; then
    build_single_elan "7"
else
    # Default mode
    if [[ -n "${TARGET_APP_OR_VER}" ]]; then
        build_single_elan "${TARGET_APP_OR_VER}"
    else
        DEFAULT_APP="$(find_elan_app)"
        build_single_elan "${DEFAULT_APP}"
    fi
fi

