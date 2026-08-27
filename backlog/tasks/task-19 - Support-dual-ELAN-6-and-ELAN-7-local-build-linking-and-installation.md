---
id: TASK-19
title: 'Support dual ELAN 6 and ELAN 7 local build, linking, and installation'
status: Done
assignee:
  - '@agent-subagent'
created_date: '2026-08-27 16:28'
updated_date: '2026-08-27 16:32'
labels: []
dependencies: []
ordinal: 19000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Enable local build scripts and utilities to link against either ELAN 6 or ELAN 7 distributions, download references on demand if local apps are missing, build dedicated JARs for both versions, and install the matching version JAR to ELAN 6 or ELAN 7 apps.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 scripts/common.sh supports discovering and linking reference JARs for both ELAN 6.x and ELAN 7.x (including remote on-demand caching if local ELAN is absent)
- [x] #2 scripts/build.sh supports building ELAN 6, ELAN 7, or both JARs (--elan 6, --elan 7, or --all)
- [x] #3 scripts/install.sh automatically detects whether the target ELAN app is ELAN 6 or ELAN 7 and installs the appropriate JAR artifact
- [x] #4 Both JARs build cleanly and all unit/integration tests pass locally
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Inspect current scripts/common.sh, scripts/build.sh, scripts/install.sh, and pom.xml.
2. Update scripts/common.sh to handle ELAN version resolution (6, 7, installed apps), on-demand downloading of reference ELAN 6.6 and 7.1 Linux tarballs into .elan-cache, linking elan.jar, and locating versioned built jars.
3. Update scripts/build.sh to support --elan 6, --elan 7, --all, copy/rename target output jars (elan6 / elan7).
4. Update scripts/install.sh to inspect target ELAN.app (version string, Info.plist, or jar) and deploy the corresponding elan6 / elan7 jar.
5. Verify build and test suite for both ELAN 6 and ELAN 7.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Validation results:
- scripts/common.sh downloads and caches reference ELAN 6.6 and 7.1 linux distributions on demand, detecting version and linking properly.
- scripts/build.sh supports --elan 6, --elan 7, and --all flags, generating cherokee-aligner-plugin-elan6-1.0.0-SNAPSHOT.jar and cherokee-aligner-plugin-elan7-1.0.0-SNAPSHOT.jar in dist/ and elan-plugin/target-out/.
- scripts/install.sh detects the target application's ELAN major version and selects the matching version JAR artifact.
- Unit and integration tests (mvn clean test) pass cleanly against both ELAN 6.6 and ELAN 7.1 jars.
- Backend pytest tests pass (6/6).
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Implemented dual ELAN 6 and ELAN 7 support across build, linking, and install scripts. Added on-demand remote reference caching for ELAN 6.6 and 7.1 Linux tarballs in scripts/common.sh, versioned multi-target builds in scripts/build.sh (--elan 6, --elan 7, --all), and automatic target version detection in scripts/install.sh. Verified clean build and full test execution against both ELAN versions.
<!-- SECTION:FINAL_SUMMARY:END -->
