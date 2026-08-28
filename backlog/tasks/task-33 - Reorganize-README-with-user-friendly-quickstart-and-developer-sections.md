---
id: TASK-33
title: Reorganize README with user-friendly quickstart and developer sections
status: Done
assignee:
  - '@antigravity'
created_date: '2026-08-28 15:25'
updated_date: '2026-08-28 15:25'
labels: []
dependencies: []
ordinal: 33000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Separate end-user ELAN plugin installation and Docker usage instructions from developer setup and build workflows in README.md.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 README presents user-facing instructions at the top with GitHub release download links, ELAN extension destination path, and Docker run instructions
- [x] #2 Docker instructions clearly state the GHCR container image name and port mapping
- [x] #3 ELAN plugin installation provides clear instructions for extracting release zips into the ELAN app bundle
- [x] #4 Developer instructions (local backend dev, compiling from source, scripts, protocol specs) are placed in a dedicated Developer Guide section
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Structure README with clear User Guide at top and Developer Guide below.
2. In User Guide, explain finding and downloading releases on GitHub, identifying ELAN 6 vs 7 zips.
3. Detail extracting release zip to ELAN extension directory (/Applications/ELAN_<version>.app/Contents/app/extensions/cherokee-aligner-ext/) or using install.sh.
4. Detail running the Docker backend container (image ghcr.io/charliemcvicker/cherokee-aligner-backend:latest on port 5050).
5. Document ELAN usage (Recognizer tab, selecting tier, start alignment).
6. Document troubleshooting and audio requirements (16-bit PCM WAV, Docker memory settings, permissions).
7. In Developer Guide, preserve dev prerequisites, local Python setup, building from source, scripts, and API specs.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Verified README structure and sections for both end-users (Releases, extraction paths, Docker commands, ELAN UI flow, troubleshooting) and developers (prereqs, local venv, build scripts, API protocol).
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Reorganized README.md into two primary sections: User & Installation Guide (at top with release downloads, ELAN directory extraction path, GHCR Docker execution, and UI steps) and Developer Guide (at bottom with local development, source builds, and API specs).
<!-- SECTION:FINAL_SUMMARY:END -->
