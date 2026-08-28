---
id: TASK-26
title: >-
  Update find_built_jar in scripts/common.sh to support versioned and release
  JAR discovery
status: Done
assignee:
  - '@antigravity'
created_date: '2026-08-28 13:42'
updated_date: '2026-08-28 13:42'
labels: []
dependencies: []
ordinal: 26000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Update find_built_jar() in scripts/common.sh to dynamically discover versioned release JARs (e.g. cherokee-aligner-plugin-elan6-*.jar, cherokee-aligner-plugin-elan7-*.jar) in dist and target directories rather than relying only on hardcoded -SNAPSHOT paths.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 find_built_jar() discovers ELAN 6 versioned JARs matching cherokee-aligner-plugin-elan6-*.jar
- [x] #2 find_built_jar() discovers ELAN 7 versioned JARs matching cherokee-aligner-plugin-elan7-*.jar
- [x] #3 find_built_jar() falls back to generic cherokee-aligner-plugin-*.jar if specific ELAN version is not found
- [x] #4 Install and build scripts continue to resolve and copy the correct JAR
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Review find_built_jar() implementation in scripts/common.sh.
2. Update find_built_jar() to search for versioned patterns (cherokee-aligner-plugin-elan6-*.jar, cherokee-aligner-plugin-elan7-*.jar) in DIST_DIR, ELAN_PLUGIN_DIR/target-out, and ELAN_PLUGIN_DIR/target.
3. Add general fallback for cherokee-aligner-plugin-*.jar excluding original-* artifacts.
4. Verify find_built_jar() behavior with dummy and existing JAR names in a test shell.
<!-- SECTION:PLAN:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Updated find_built_jar() in scripts/common.sh to dynamically discover release and versioned JARs (cherokee-aligner-plugin-elan6-*.jar, cherokee-aligner-plugin-elan7-*.jar, and fallback cherokee-aligner-plugin-*.jar) prioritized across dist/, target-out/, and target/ directories using version sorting.
<!-- SECTION:FINAL_SUMMARY:END -->
