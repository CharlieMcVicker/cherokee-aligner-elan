---
id: TASK-21
title: Enhance CI/CD workflow with ELAN reference caching and retry resilience
status: Done
assignee:
  - '@agent'
created_date: '2026-08-27 19:11'
updated_date: '2026-08-27 19:11'
labels: []
dependencies: []
ordinal: 21000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Improve GitHub Actions CI pipeline reliability by adding caching for reference ELAN JARs and curl retry mechanisms, then push changes to trigger CI run.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 GitHub Actions ci.yml uses actions/cache for reference ELAN binaries and curl retries
- [x] #2 Local plugin build and tests continue to pass cleanly
- [x] #3 Changes committed and pushed to remote to rerun CI
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Update .github/workflows/ci.yml to incorporate actions/cache@v4 for elan-plugin/lib/elan.jar keyed by matrix elan_version and add curl retry flags.
2. Verify local builds and tests pass cleanly with scripts/build.sh --all.
3. Commit and push the changes to remote main branch to rerun GitHub Actions CI.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Added actions/cache@v4 and curl retry flags in ci.yml and release.yml. Verified local dual ELAN 6/7 builds and Java unit/integration tests with ./scripts/build.sh --all and mvn clean test.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Enhanced GitHub Actions CI and release workflows by adding actions/cache@v4 caching for ELAN reference JARs and adding retry resilience to download commands. Verified build & test suite and pushed changes to main.
<!-- SECTION:FINAL_SUMMARY:END -->
