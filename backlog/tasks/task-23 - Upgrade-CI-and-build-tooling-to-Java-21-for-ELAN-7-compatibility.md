---
id: TASK-23
title: Upgrade CI and build tooling to Java 21 for ELAN 7 compatibility
status: Done
assignee:
  - '@agent'
created_date: '2026-08-28 13:21'
updated_date: '2026-08-28 13:22'
labels: []
dependencies: []
ordinal: 23000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
ELAN 7.1 bytecode requires class file version 65.0 (Java 21). Update CI workflow, release workflow, scripts, and Maven compiler settings to use Java 21 for ELAN 7 builds.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 CI matrix builds ELAN 6.6 with Java 17 and ELAN 7.1 with Java 21 (or uses Java 21 across builds)
- [x] #2 Release workflow builds ELAN 7.1 with Java 21
- [x] #3 Local dual build scripts support Java 21
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Check ELAN 6.6 and 7.1 Java compatibility and verify whether Java 21 can compile both or if Java matrix per version is needed.
2. Update .github/workflows/ci.yml matrix with java_version (Java 17 for ELAN 6.6, Java 21 for ELAN 7.1).
3. Update .github/workflows/release.yml to use Java 21 when building for ELAN 7.1.
4. Verify pom.xml and scripts/build.sh work cleanly across both versions.
5. Verify tests and packaging pass.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Updated CI workflow to set up Java per matrix version (Java 17 for ELAN 6.6, Java 21 for ELAN 7.1) and cache key v2 with existence validation. Updated release workflow to use Java 21.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Resolved the ELAN 7.1 compilation failure caused by Java 21 bytecode (class version 65.0) being built under Java 17. Updated .github/workflows/ci.yml to matrix Java 17/21 and .github/workflows/release.yml to Java 21, and refreshed cache validation.
<!-- SECTION:FINAL_SUMMARY:END -->
