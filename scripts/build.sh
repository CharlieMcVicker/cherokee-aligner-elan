#!/usr/bin/env bash
set -euo pipefail

# Script to build the Cherokee Forced-Alignment ELAN Plugin JAR
# Usage: ./scripts/build.sh [/path/to/ELAN.app]

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

ELAN_APP_ARG="${1:-}"

# 1. Locate ELAN Application Bundle
ELAN_APP="$(find_elan_app "${ELAN_APP_ARG}")"
echo "==> Using ELAN installation for dependencies: ${ELAN_APP}"

# 2. Locate and link elan.jar into elan-plugin/lib/elan.jar
link_elan_jar "${ELAN_APP}"

# 3. Build the plugin jar via Maven
echo "==> Building ELAN Plugin with Maven..."
(cd "${ELAN_PLUGIN_DIR}" && mvn clean package -DskipTests)

TARGET_JAR="$(find_built_jar)"
if [[ -z "${TARGET_JAR}" || ! -f "${TARGET_JAR}" ]]; then
    echo "Error: Built JAR not found." >&2
    exit 1
fi

echo "==> Build complete: ${TARGET_JAR}"
