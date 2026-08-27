---
id: TASK-12
title: 'Refactor backend into app, model, and orthography modules'
status: Done
assignee:
  - '@agent'
created_date: '2026-08-27 14:59'
updated_date: '2026-08-27 15:00'
labels: []
dependencies: []
ordinal: 12000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Split the backend logic into three distinct files: app.py for API endpoints, a model execution module for audio alignment/model inference, and an orthography module for syllabary and phonetic transformations.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 app.py retains Flask endpoints and delegates alignment/model and orthography operations to dedicated modules
- [x] #2 Create a dedicated model/alignment runner module
- [x] #3 Create a dedicated orthography module with syllabary-to-phonetic conversions and extensible structure for future writing system conversions
- [x] #4 All existing tests pass or are updated to reflect the new structure
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Create backend/orthography.py to handle Cherokee orthography conversions (syllabary to phonetics using cherokee_to_bad_phonetics and stub/placeholder for future library conversion).\n2. Create backend/model_runner.py (or aligner_runner.py) to encapsulate audio normalization, model loading, and CTC/DTW alignment execution.\n3. Refactor backend/app.py to keep Flask routes /health and /v1/align/segment, importing and delegating to model_runner and orthography modules.\n4. Update backend tests and ensure pytest passes seamlessly.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Refactored backend architecture into app.py (endpoints), model_runner.py (audio normalization and model alignment execution), and orthography.py (syllabary/phonetics transforms and placeholder hook for target script conversions). All 6 pytest test cases pass.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Decoupled backend code into app.py, model_runner.py, and orthography.py. Verified with pytest (6/6 tests passing).
<!-- SECTION:FINAL_SUMMARY:END -->
