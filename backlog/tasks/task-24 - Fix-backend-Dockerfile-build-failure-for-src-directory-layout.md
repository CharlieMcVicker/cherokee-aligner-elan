---
id: TASK-24
title: Fix backend Dockerfile build failure for src directory layout
status: Done
assignee:
  - '@antigravity'
created_date: '2026-08-28 13:26'
updated_date: '2026-08-28 13:31'
labels: []
dependencies: []
ordinal: 24000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Fix backend Dockerfile build failure during 'pip install .' where setuptools cannot find src directory when only pyproject.toml is copied into the build layer.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 backend Dockerfile builds successfully with docker build
- [x] #2 Backend test suite passes inside the built Docker container
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Update backend/Dockerfile to create the src directory (mkdir -p src) before running pip install . with pyproject.toml.\n2. Build the Docker image locally to verify the build succeeds.\n3. Run the test suite inside the built Docker container.\n4. Verify all criteria and finalize task.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Verified local docker build (docker build -t cherokee-aligner:test .) and container pytest execution (docker run --rm cherokee-aligner:test pytest tests, 6/6 passed).
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Resolved the CI backend build failure caused by setuptools requiring a src directory when reading pyproject.toml package find configuration. Updated backend/Dockerfile to create the src directory (mkdir -p src) prior to running pip install . during dependency installation layer. Verified with docker build and in-container pytest run (6 passed).
<!-- SECTION:FINAL_SUMMARY:END -->
