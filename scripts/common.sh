#!/usr/bin/env bash
set -euo pipefail

# Shared utility functions for Cherokee ELAN plugin scripts

REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ELAN_PLUGIN_DIR="${REPO_DIR}/elan-plugin"
CACHE_DIR="${REPO_DIR}/.elan-cache"
DIST_DIR="${REPO_DIR}/dist"
CMDI_FILE1="${ELAN_PLUGIN_DIR}/src/main/resources/cherokee-aligner.cmdi"
CMDI_FILE2="${ELAN_PLUGIN_DIR}/src/main/resources/recognizer.cmdi"

ELAN6_URL="https://www.mpi.nl/tools/elan/ELAN_6-6_linux.tar.gz"
ELAN7_URL="https://www.mpi.nl/tools/elan/ELAN_7-1_linux.tar.gz"

ensure_elan_reference() {
    local version="$1" # "6", "6.6", "7", "7.1"
    mkdir -p "${CACHE_DIR}"

    local major=""
    local url=""
    local extract_dir=""
    local jar_name=""

    case "${version}" in
        6|6.*)
            major="6"
            url="${ELAN6_URL}"
            extract_dir="${CACHE_DIR}/elan-6.6"
            jar_name="elan-6.6.jar"
            ;;
        7|7.*)
            major="7"
            url="${ELAN7_URL}"
            extract_dir="${CACHE_DIR}/elan-7.1"
            jar_name="elan-7.1.jar"
            ;;
        *)
            echo "Error: Unknown ELAN version: ${version}" >&2
            return 1
            ;;
    esac

    local cached_jar="${extract_dir}/${jar_name}"
    if [[ -f "${cached_jar}" ]]; then
        echo "${cached_jar}"
        return 0
    fi

    echo "==> Reference ELAN ${major} not found in cache. Downloading from ${url}..." >&2
    local tmp_tar="${CACHE_DIR}/elan_${major}_download.tar.gz"
    mkdir -p "${extract_dir}"

    if ! curl -sSL -f "${url}" -o "${tmp_tar}"; then
        echo "Error: Failed to download reference ELAN from ${url}" >&2
        rm -f "${tmp_tar}"
        return 1
    fi

    echo "==> Extracting reference elan JAR..." >&2
    tar -xzf "${tmp_tar}" -C "${extract_dir}" --strip-components=3 "ELAN_${major}"*/lib/app/"${jar_name}" 2>/dev/null || \
    tar -xzf "${tmp_tar}" -C "${extract_dir}" 2>/dev/null || true

    rm -f "${tmp_tar}"

    if [[ ! -f "${cached_jar}" ]]; then
        local found
        found="$(find "${extract_dir}" -iname "elan*.jar" ! -iname "*plugin*" ! -iname "*ext*" | head -n 1 || true)"
        if [[ -n "${found}" && -f "${found}" ]]; then
            cp "${found}" "${cached_jar}"
        fi
    fi

    if [[ -f "${cached_jar}" ]]; then
        echo "==> Cached reference ELAN ${major} jar at ${cached_jar}" >&2
        echo "${cached_jar}"
        return 0
    else
        echo "Error: Could not extract elan.jar from ${url}" >&2
        return 1
    fi
}

detect_elan_version() {
    local target="$1" # Can be version string ("6", "7", "6.6", etc.), path to ELAN.app, or path to elan.jar

    if [[ "${target}" =~ ^6(\..*)?$ ]]; then
        echo "6"
        return 0
    elif [[ "${target}" =~ ^7(\..*)?$ ]]; then
        echo "7"
        return 0
    fi

    if [[ -d "${target}" ]]; then
        local plist="${target}/Contents/Info.plist"
        if [[ -f "${plist}" ]]; then
            local version_str
            version_str="$(defaults read "${plist}" CFBundleShortVersionString 2>/dev/null || true)"
            if [[ -z "${version_str}" ]]; then
                version_str="$(defaults read "${plist}" CFBundleVersion 2>/dev/null || true)"
            fi
            if [[ -z "${version_str}" ]]; then
                version_str="$(grep -A1 "CFBundleShortVersionString" "${plist}" 2>/dev/null | grep -o '>[^<]*<' | tr -d '><' || true)"
            fi
            if [[ "${version_str}" =~ ^6 ]]; then
                echo "6"
                return 0
            elif [[ "${version_str}" =~ ^7 ]]; then
                echo "7"
                return 0
            fi
        fi

        local bundle_name
        bundle_name="$(basename "${target}")"
        if [[ "${bundle_name}" =~ 6 ]]; then
            echo "6"
            return 0
        elif [[ "${bundle_name}" =~ 7 ]]; then
            echo "7"
            return 0
        fi

        local inside_jar
        inside_jar="$(find "${target}" -iname "elan*.jar" ! -iname "*plugin*" ! -iname "*ext*" 2>/dev/null | head -n 1 || true)"
        if [[ -n "${inside_jar}" ]]; then
            detect_elan_version "${inside_jar}"
            return 0
        fi
    elif [[ -f "${target}" ]]; then
        local jar_name
        jar_name="$(basename "${target}")"
        if [[ "${jar_name}" =~ 6 ]]; then
            echo "6"
            return 0
        elif [[ "${jar_name}" =~ 7 ]]; then
            echo "7"
            return 0
        fi
    fi

    echo "unknown"
    return 0
}

find_elan_app() {
    local requested_app="${1:-}"
    if [[ -n "${requested_app}" ]]; then
        if [[ "${requested_app}" =~ ^[67](\.[0-9]+)?$ ]]; then
            local match_app
            match_app="$(find /Applications -maxdepth 1 -iname "ELAN*${requested_app}*.app" 2>/dev/null | sort -V | tail -n 1 || true)"
            if [[ -n "${match_app}" && -d "${match_app}" ]]; then
                echo "${match_app}"
                return 0
            fi
            echo "${requested_app}"
            return 0
        fi

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
    if [[ -n "${found_app}" && -d "${found_app}" ]]; then
        echo "${found_app}"
        return 0
    fi

    echo "7"
}

find_built_jar() {
    local requested_version="${1:-}"

    if [[ "${requested_version}" =~ 6 ]]; then
        for path in \
            "${DIST_DIR}/cherokee-aligner-plugin-elan6-1.0.0-SNAPSHOT.jar" \
            "${ELAN_PLUGIN_DIR}/target-out/cherokee-aligner-plugin-elan6-1.0.0-SNAPSHOT.jar" \
            "${ELAN_PLUGIN_DIR}/target/cherokee-aligner-plugin-elan6-1.0.0-SNAPSHOT.jar"; do
            if [[ -f "${path}" ]]; then
                echo "${path}"
                return 0
            fi
        done
    elif [[ "${requested_version}" =~ 7 ]]; then
        for path in \
            "${DIST_DIR}/cherokee-aligner-plugin-elan7-1.0.0-SNAPSHOT.jar" \
            "${ELAN_PLUGIN_DIR}/target-out/cherokee-aligner-plugin-elan7-1.0.0-SNAPSHOT.jar" \
            "${ELAN_PLUGIN_DIR}/target/cherokee-aligner-plugin-elan7-1.0.0-SNAPSHOT.jar"; do
            if [[ -f "${path}" ]]; then
                echo "${path}"
                return 0
            fi
        done
    fi

    for path in \
        "${DIST_DIR}/cherokee-aligner-plugin-1.0.0-SNAPSHOT.jar" \
        "${ELAN_PLUGIN_DIR}/target-out/cherokee-aligner-plugin-1.0.0-SNAPSHOT.jar" \
        "${ELAN_PLUGIN_DIR}/target/cherokee-aligner-plugin-1.0.0-SNAPSHOT.jar"; do
        if [[ -f "${path}" ]]; then
            echo "${path}"
            return 0
        fi
    done

    echo ""
}

link_elan_jar() {
    local target="${1:-}"
    mkdir -p "${ELAN_PLUGIN_DIR}/lib"
    local found_jar=""

    if [[ -z "${target}" ]]; then
        target="$(find_elan_app)"
    fi

    if [[ "${target}" =~ ^[67](\.[0-9]+)?$ ]]; then
        found_jar="$(ensure_elan_reference "${target}")"
    elif [[ -d "${target}" ]]; then
        if [[ -f "${target}/Contents/app/elan.jar" ]]; then
            found_jar="${target}/Contents/app/elan.jar"
        elif [[ -f "${target}/Contents/Java/elan.jar" ]]; then
            found_jar="${target}/Contents/Java/elan.jar"
        elif [[ -f "${target}/Contents/Resources/app/elan.jar" ]]; then
            found_jar="${target}/Contents/Resources/app/elan.jar"
        else
            found_jar="$(find "${target}" -iname "elan*.jar" ! -iname "*plugin*" ! -iname "*ext*" 2>/dev/null | head -n 1 || true)"
        fi
    elif [[ -f "${target}" ]]; then
        found_jar="${target}"
    fi

    if [[ -z "${found_jar}" || ! -f "${found_jar}" ]]; then
        local detected_ver
        detected_ver="$(detect_elan_version "${target}")"
        if [[ "${detected_ver}" == "6" || "${detected_ver}" == "7" ]]; then
            found_jar="$(ensure_elan_reference "${detected_ver}")"
        else
            found_jar="$(ensure_elan_reference "7")"
        fi
    fi

    if [[ -n "${found_jar}" && -f "${found_jar}" ]]; then
        echo "==> Found ELAN jar: ${found_jar}"
        echo "==> Linking to ${ELAN_PLUGIN_DIR}/lib/elan.jar..."
        ln -sf "${found_jar}" "${ELAN_PLUGIN_DIR}/lib/elan.jar"
    elif [[ ! -f "${ELAN_PLUGIN_DIR}/lib/elan.jar" ]]; then
        echo "Error: Could not find or download elan.jar for ${target}." >&2
        return 1
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
