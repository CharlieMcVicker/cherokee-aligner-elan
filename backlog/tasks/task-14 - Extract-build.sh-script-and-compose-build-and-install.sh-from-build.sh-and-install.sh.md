---
id: TASK-14
title: >-
  Extract build.sh script and compose build-and-install.sh from build.sh and
  install.sh
status: Done
assignee:
  - '@antigravity'
created_date: '2026-08-27 16:04'
updated_date: '2026-08-27 16:05'
labels: []
dependencies: []
ordinal: 14000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Extract the plugin compilation and elan.jar linking logic into scripts/build.sh so users can build without installing, and update scripts/build-and-install.sh to sequentially invoke build.sh and install.sh.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 scripts/build.sh links elan.jar and compiles the plugin JAR via Maven without installing
- [x] #2 scripts/build-and-install.sh invokes scripts/build.sh and then scripts/install.sh
- [x] #3 README.md is updated to document scripts/build.sh
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Create scripts/build.sh to locate ELAN/elan.jar and run Maven package.
2. Refactor scripts/build-and-install.sh to call build.sh followed by install.sh.
3. Update README.md to document scripts/build.sh.
4. Verify by running build.sh and build-and-install.sh.
<!-- SECTION:PLAN:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Extracted scripts/build.sh for linking elan.jar and compiling the shaded plugin JAR via Maven. Updated scripts/build-and-install.sh to compose build.sh followed by install.sh. Updated README.md to document the separate build, install, and combined workflows.
<!-- SECTION:FINAL_SUMMARY:END -->
