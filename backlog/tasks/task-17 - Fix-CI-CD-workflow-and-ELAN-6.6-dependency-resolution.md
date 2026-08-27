---
id: TASK-17
title: Fix CI/CD workflow and ELAN 6.6 dependency resolution
status: Done
assignee:
  - '@agent'
created_date: '2026-08-27 16:16'
updated_date: '2026-08-27 16:17'
labels: []
dependencies: []
ordinal: 17000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Fix CI/CD pipeline failure by updating GitHub Actions to fetch ELAN 6.6 reference binaries, resolving compilation incompatibilities, aligning build artifact paths, and establishing automated CI for pull requests and main pushes.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 GitHub Actions workflows download ELAN 6.6 Linux distribution (ELAN_6-6_linux.tar.gz) and extract elan-6.6.jar to elan-plugin/lib/elan.jar
- [x] #2 CherokeeAlignerRecognizer compiles cleanly against ELAN 6.6 and 7.x APIs using java.util.List interface
- [x] #3 GitHub release workflow correctly references generated artifact JAR in elan-plugin/target-out/ (or target/)
- [x] #4 Automated CI workflow runs Java plugin build and Python backend test suite on pull requests and pushes to main
- [x] #5 All tests pass successfully in Maven and pytest
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Update CherokeeAlignerRecognizer.java to use List<RSelection> for polymorphic compatibility across ELAN 6.6 and 7.x APIs.
2. Update .github/workflows/release.yml to fetch ELAN 6.6 Linux package (ELAN_6-6_linux.tar.gz) and extract elan-6.6.jar to elan-plugin/lib/elan.jar.
3. Fix release workflow artifact path to point to target-out/cherokee-aligner-plugin-1.0.0-SNAPSHOT.jar.
4. Create .github/workflows/ci.yml to run Maven package/test and pytest suite on pull requests and pushes to main.
5. Verify build and test execution with both ELAN 6.6 and ELAN 7.1 jars locally and ensure all tests pass.
<!-- SECTION:PLAN:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Updated GitHub Actions release and CI workflows to download ELAN 6.6 Linux distribution for dependency resolution during automated builds. Updated CherokeeAlignerRecognizer to use the standard List interface for input segments, ensuring seamless compilation across ELAN 6.6 and 7.x APIs. Fixed release artifact paths and added comprehensive CI workflow covering Java Maven tests and Python backend pytest suites.
<!-- SECTION:FINAL_SUMMARY:END -->
