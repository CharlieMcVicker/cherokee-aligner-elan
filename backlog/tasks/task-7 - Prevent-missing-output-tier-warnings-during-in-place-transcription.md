---
id: TASK-7
title: Prevent missing output tier warnings during in-place transcription
status: Done
assignee:
  - '@antigravity'
created_date: '2026-08-26 21:54'
updated_date: '2026-08-26 21:59'
labels: []
dependencies: []
ordinal: 7000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
When hitting Start to transcribe/align in ELAN, ELAN reports errors/warnings that no output tiers were created because the plugin edits tiers in place rather than creating new output tiers. Adjust the recognizer configuration/API interaction or output tier handling so ELAN does not warn or error on missing output tiers.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Hitting Start in ELAN recognizer does not show warnings or error popups about failing to create output tiers
- [x] #2 In-place tier editing continues to function as expected
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Create CherokeeAlignerPanel (custom JPanel implementing ParamPreferences) integrating ELAN's AbstractSelectionPanel for tier/selection selection alongside script type and target tier options.
2. Update CherokeeAlignerRecognizer.getControlPanel() to return CherokeeAlignerPanel, preventing ELAN's ParamPanelContainer from triggering spurious missing output tier / no output warnings.
3. Update recognizer and panel parameters handling and validation.
4. Update unit tests to verify control panel and in-place tier alignment.
5. Run full test suite to verify no regressions.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Created CherokeeAlignerPanel to embed in ELAN's recognizer tab. Because CherokeeAlignerRecognizer returns its own custom control panel (rather than null), ELAN's AbstractRecognizerPanel bypasses ParamPanelContainer's checkOutput() logic which previously triggered 'No output files have been created' and 'Failed to create output tiers' warnings on completion of in-place editing.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Implemented custom CherokeeAlignerPanel (JPanel + ParamPreferences) in CherokeeAlignerRecognizer.getControlPanel(), resolving ELAN's checkOutput() spurious warnings regarding missing output tiers while preserving in-place transcription editing. Verified via unit tests (CherokeeAlignerRecognizerTest).
<!-- SECTION:FINAL_SUMMARY:END -->
