---
id: TASK-31
title: Streamline ELAN Recognizer UI to Core Input/Output Tier Workflow
status: Done
assignee:
  - '@myself'
created_date: '2026-08-28 14:46'
updated_date: '2026-08-28 14:47'
labels: []
dependencies: []
documentation:
  - >-
    backlog/docs/guides/ui-audit-ux-simplification/doc-1 -
    UI-Audit-and-UX-Simplification.md
ordinal: 31000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Refactor CherokeeAlignerPanel, CherokeeAlignerRecognizer, and recognizer.cmdi to strictly support the core workflow (select existing input sentence tier -> select existing output word tier -> align). Remove broken selection mode, remove synthetic auto-create tier options, remove dead dialog classes, and update tests.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Input mode strictly requires tier selection in recognizer.cmdi and control panel validation
- [x] #2 Target tier dropdown strictly contains existing tiers from the transcription, removing [Auto-create: words] and free-text tier addition
- [x] #3 Remove unused AlignmentOptionsDialog and cleanly handle missing target tier validation
- [x] #4 Maven compilation and unit tests pass in elan-plugin
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Update recognizer.cmdi to strictly configure tier input.
2. Refactor CherokeeAlignerPanel: remove AUTO_CREATE_WORDS, populate targetTierCombo strictly with existing transcription tiers (default to 'words' if present else first tier), remove free-text tier adding in setTargetTierName, and validate valid source & target tier selections in validateParameters().
3. Refactor CherokeeAlignerRecognizer: remove auto-create tier logic & raw time-selection fallback logic, validate target tier existence in transcription before processing.
4. Delete unused legacy AlignmentOptionsDialog.java.
5. Update unit tests in CherokeeAlignerRecognizerTest and CherokeeAlignerPanelTest (if any) to align with new behavior.
6. Verify build and test suite with 'mvn clean test' in elan-plugin.
<!-- SECTION:PLAN:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Streamlined ELAN recognizer UI and backend to the core input/output tier workflow:
- Configured recognizer.cmdi to strictly require source tier input.
- Refactored CherokeeAlignerPanel to remove AUTO_CREATE_WORDS and free-text tier entry, populating the target tier dropdown strictly with existing transcription tiers (defaulting to 'words' or first available tier), and validating both source and target tier selections.
- Refactored CherokeeAlignerRecognizer to remove auto-create tier fallback logic and validate that the target tier exists in the transcription.
- Removed unused legacy AlignmentOptionsDialog.
- Updated CherokeeAlignerRecognizerTest to verify new tier selection, panel validation, and missing tier handling. Verified with 'mvn clean test' (all 10 tests passed).
<!-- SECTION:FINAL_SUMMARY:END -->
