---
id: TASK-9
title: Refine Recognizer UI with Server URL and dynamic Target Tier selector dropdown
status: Done
assignee:
  - '@antigravity'
created_date: '2026-08-27 13:13'
updated_date: '2026-08-27 13:16'
labels: []
dependencies: []
ordinal: 9000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Update CherokeeAlignerPanel and CherokeeAlignerRecognizer to implement refined UI requirements: Connection Settings panel with Server URL input & validation, dynamic Target Words Tier dropdown populated with active document tiers and auto-create options, and updated layout flow.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Server URL field is positioned in Connection Settings panel with default 'http://localhost:5050'
- [x] #2 Server URL validation enforces valid http/https format and raises RecognizerConfigurationException when invalid
- [x] #3 Target Words Tier is a JComboBox dropdown dynamically populated with active transcription tiers and '[Auto-create: words]' option
- [x] #4 AlignmentClient and Recognizer use configured Server URL during alignment runs
- [x] #5 Parameter preferences support saving and restoring server_url, script_type, and target_tier
- [x] #6 Tests verify panel initialization, validation, parameter loading, and tier selection
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Update CherokeeAlignerPanel.java with Connection Settings (Server URL field & validation), Input Tier/Selection, and Alignment Settings (Script Type & dynamic Target Words Tier JComboBox).
2. Update CherokeeAlignerRecognizer.java to extract transcription tiers, handle server_url parameter & client creation, and support target tier resolution.
3. Update recognizer CMDI descriptors with server_url textparam.
4. Add comprehensive unit tests for UI layout, URL validation, dynamic tier dropdown, and parameter preferences.
5. Run full test suite and verify build.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Implemented Connection Settings panel with Server URL input and URI validation. Replaced target tier text field with dynamic JComboBox populated with active ELAN transcription tiers and auto-create option. Updated CMDI descriptors and verified with Maven unit tests.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Refined Cherokee ELAN Recognizer UI with Connection Settings panel (Server URL field & http/https validation), dynamic Target Words Tier dropdown selector populated with document tiers and auto-create options, and full parameter persistence. Verified all test suites pass with Maven.
<!-- SECTION:FINAL_SUMMARY:END -->
