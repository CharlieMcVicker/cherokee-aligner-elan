---
id: TASK-27
title: Package release artifacts as ELAN 6 and ELAN 7 zips with descriptors
status: Done
assignee:
  - '@antigravity'
created_date: '2026-08-28 14:08'
updated_date: '2026-08-28 14:10'
labels: []
dependencies: []
ordinal: 27000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Update artifact packaging in CI and release workflows and build scripts to produce zip archives for ELAN 6 and ELAN 7 (cherokee-aligner-elan-6.zip and cherokee-aligner-elan-7.zip) containing the plugin JAR and CMDI descriptor files, alongside source code.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Zip files for ELAN 6 and ELAN 7 are generated named cherokee-aligner-elan-6.zip and cherokee-aligner-elan-7.zip
- [x] #2 Each zip archive contains at its root: the compiled plugin jar, cherokee-aligner.cmdi, and recognizer.cmdi
- [x] #3 Release workflow publishes the ELAN 6 and ELAN 7 zips as release assets along with GitHub source archives
- [x] #4 CI workflow packages and verifies the zip artifacts
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Update .github/workflows/release.yml to package cherokee-aligner-elan-6.zip and cherokee-aligner-elan-7.zip containing the plugin jar and both CMDI descriptor files at root, uploading these zips in build-plugin and publishing them in release-github.
2. Update .github/workflows/ci.yml to package and verify the zip archives for both ELAN 6 and ELAN 7 matrices.
3. Update scripts/build.sh and scripts/common.sh so that local builds also generate cherokee-aligner-elan-6.zip and cherokee-aligner-elan-7.zip in dist/.
4. Update README.md and documentation to describe the zip artifact distribution and installation steps.
5. Verify build script and zip contents locally.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Verified zip packaging via ./scripts/build.sh --all. Inspected dist/cherokee-aligner-elan-6.zip and dist/cherokee-aligner-elan-7.zip using unzip -l. Tested install.sh extracting from zip archive onto mock ELAN bundle.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Updated CI and Release workflows to package plugin JARs with both cherokee-aligner.cmdi and recognizer.cmdi at the root of cherokee-aligner-elan-6.zip and cherokee-aligner-elan-7.zip. Configured GitHub Releases to publish the zips alongside default source archives. Updated scripts/build.sh and scripts/install.sh to generate and support extension zip archives, and updated README.md documentation.
<!-- SECTION:FINAL_SUMMARY:END -->
