---
id: TASK-18
title: Add ELAN 7 matrix build to CI/CD and publish dual ELAN 6 and 7 release assets
status: Done
assignee:
  - '@agent'
created_date: '2026-08-27 16:18'
updated_date: '2026-08-27 16:19'
labels: []
dependencies: []
ordinal: 18000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Extend CI/CD workflows to build and test against both ELAN 6.6 and ELAN 7.1 distributions, and include separate version-labeled plugin JARs (ELAN 6.x and ELAN 7.x) in GitHub Releases.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 CI workflow builds and runs test suites across both ELAN 6.6 and ELAN 7.1 reference environments
- [x] #2 Release workflow compiles and packages dedicated JARs for ELAN 6.6 (cherokee-aligner-plugin-elan6-1.0.0-SNAPSHOT.jar) and ELAN 7.1 (cherokee-aligner-plugin-elan7-1.0.0-SNAPSHOT.jar)
- [x] #3 Both ELAN 6 and ELAN 7 JARs along with CMDI descriptor files are attached to the GitHub release
- [x] #4 All CI checks and Maven build packaging steps succeed for both versions
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Update .github/workflows/ci.yml with a matrix strategy to build and test the Java plugin against ELAN 6.6 and ELAN 7.1.
2. Update .github/workflows/release.yml to build plugin artifacts against both ELAN 6.6 and ELAN 7.1, producing cherokee-aligner-plugin-elan6-1.0.0-SNAPSHOT.jar and cherokee-aligner-plugin-elan7-1.0.0-SNAPSHOT.jar (plus standard default jar).
3. Update release step to upload both ELAN 6.x and ELAN 7.x JARs along with CMDI files to GitHub Release assets.
4. Verify workflows and confirm local build and test execution for both ELAN versions.
<!-- SECTION:PLAN:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Updated CI and Release workflows to support dual ELAN 6.6 and ELAN 7.1 builds. CI runs a matrix testing both ELAN versions against all unit/integration tests. Release workflow builds and uploads both cherokee-aligner-plugin-elan6-1.0.0-SNAPSHOT.jar and cherokee-aligner-plugin-elan7-1.0.0-SNAPSHOT.jar (as well as default jar) and CMDI descriptor files.
<!-- SECTION:FINAL_SUMMARY:END -->
