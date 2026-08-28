---
id: TASK-29
title: Fix zip destination path during CI and Release workflow packaging
status: Done
assignee:
  - '@antigravity'
created_date: '2026-08-28 14:31'
updated_date: '2026-08-28 14:31'
labels: []
dependencies: []
ordinal: 29000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
In CI and Release GitHub Actions workflows, staging occurs in /tmp/..., but the zip packaging command uses relative path ../../dist/${ZIP_NAME}, which resolves to /dist/${ZIP_NAME} and fails with 'No such file or directory'. Fix zip destination path to target the repository dist directory reliably.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Release workflow packaging step outputs extension zip into repository dist directory without I/O error
- [x] #2 CI workflow packaging step outputs extension zip into repository dist directory without I/O error
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Inspect .github/workflows/ci.yml and .github/workflows/release.yml.
2. In both workflows, ensure the repository dist directory path is resolved reliably (e.g., using GITHUB_WORKSPACE or absolute path) when running zip from the /tmp staging directory.
3. Test/verify packaging commands locally or via shell verification.
4. Verify all workflows and scripts are consistent.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Updated .github/workflows/ci.yml and .github/workflows/release.yml to define DIST_DIR="${GITHUB_WORKSPACE:-$(pwd)}/dist" and output zip archives to "${DIST_DIR}/${ZIP_NAME}" and inspect with unzip -l "${DIST_DIR}/${ZIP_NAME}". Verified build and packaging pipeline.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Fixed zip packaging destination path in .github/workflows/ci.yml and .github/workflows/release.yml to use the absolute DIST_DIR path instead of the invalid relative path ../../dist/ when executed from /tmp staging directories.
<!-- SECTION:FINAL_SUMMARY:END -->
