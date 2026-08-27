---
id: TASK-13
title: Decouple plugin build and install scripts and refine installation docs
status: Done
assignee:
  - '@antigravity'
created_date: '2026-08-27 15:56'
updated_date: '2026-08-27 15:57'
labels: []
dependencies: []
ordinal: 13000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Separate the ELAN plugin installation from the Maven build step by providing modular scripts (install.sh, build-and-install.sh, and shared utilities). Update documentation and scripts to handle ELAN application bundle installation cleanly, add permission troubleshooting guidance, and reflect the decoupled install workflows.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 scripts/install.sh installs pre-built plugin JAR and .cmdi descriptors to ELAN without running Maven build
- [x] #2 scripts/build-and-install.sh compiles the plugin via Maven and invokes install.sh
- [x] #3 scripts/update-plugin.sh delegates to build-and-install.sh or maintains backward compatibility
- [x] #4 Scripts check write permissions on ELAN extensions directory and output actionable chown advice on failure
- [x] #5 README.md is updated with decoupled installation instructions and macOS permission troubleshooting
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Create scripts/common.sh for ELAN bundle detection, elan.jar linking, and permission checks with actionable chown advice.
2. Create scripts/install.sh to install pre-built JAR and descriptor files without compiling.
3. Create scripts/build-and-install.sh to compile via Maven and call install.sh.
4. Update scripts/update-plugin.sh to delegate to build-and-install.sh for backward compatibility.
5. Update README.md with decoupled script usage and macOS permission troubleshooting.
<!-- SECTION:PLAN:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Decoupled ELAN plugin installation into scripts/common.sh, scripts/install.sh (pre-built install), scripts/build-and-install.sh (Maven build + install), and scripts/update-plugin.sh (legacy entrypoint). Added write permission and ownership detection with actionable sudo chown guidance. Updated README.md with system prerequisites (Python 3.12, FFmpeg, JDK 17, Maven), decoupled install instructions, and macOS permission troubleshooting. Created backend/.dockerignore and .dockerignore to exclude virtualenvs and build caches.
<!-- SECTION:FINAL_SUMMARY:END -->
