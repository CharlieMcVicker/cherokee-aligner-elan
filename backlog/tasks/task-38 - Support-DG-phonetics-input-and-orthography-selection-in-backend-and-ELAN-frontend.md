---
id: TASK-38
title: >-
  Support DG phonetics input and orthography selection in backend and ELAN
  frontend
status: Done
assignee:
  - '@agent'
created_date: '2026-09-24 13:31'
updated_date: '2026-09-24 13:34'
labels: []
dependencies: []
ordinal: 35000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Users transcribe Cherokee audio in Syllabary, Durbin Feeling (DG) phonetics, or Cherokee Phonetic Alphabet (TTH) phonetics. Currently, the backend model runner only handles 'syllabary' or 'latin' (which treated input as raw phonetics without converting DG d/g stops to TTH t/th). We need full support for 'syllabary', 'dg', and 'tth' orthographies across the backend model runner, API, and the ELAN plugin UI/CMDI descriptors so users can select their exact transcription system.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Backend model runner supports 'syllabary', 'dg', and 'tth' (as well as backwards-compatible 'latin' alias for dg) in run_alignment
- [x] #2 When script_type is 'dg' or 'latin', input is converted from DG to TTH and aligned with enforce_phonotactics=False
- [x] #3 When script_type is 'tth', input is cleaned and aligned with enforce_phonotactics=False
- [x] #4 When script_type is 'syllabary', input is converted from SYLLABARY to TTH and aligned with enforce_phonotactics=True
- [x] #5 ELAN plugin CherokeeAlignerPanel UI dropdown offers 'syllabary', 'dg', and 'tth' options
- [x] #6 CMDI descriptors (cherokee-aligner.cmdi and recognizer.cmdi) include convoc='syllabary dg tth latin'
- [x] #7 Backend unit tests and ELAN plugin unit tests pass cleanly
- [x] #8 Local docker test image builds cleanly and passes containerized pytest
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Update backend/src/model_runner.py to handle 'syllabary', 'dg', 'latin', and 'tth' script_type options: convert 'syllabary' from SYLLABARY to TTH (enforce_phonotactics=True), convert 'dg' or 'latin' from DG to TTH (enforce_phonotactics=False), and clean 'tth' (enforce_phonotactics=False).
2. Update backend/tests/test_aligner.py to test all script_type variants ('syllabary', 'dg', 'latin', 'tth').
3. Update ELAN plugin CherokeeAlignerPanel.java to provide 'syllabary', 'dg', and 'tth' in the script type dropdown and handle preferences accordingly.
4. Update ELAN CMDI descriptors (cherokee-aligner.cmdi and recognizer.cmdi) to list convoc='syllabary dg tth latin'.
5. Update ELAN Java unit tests in CherokeeAlignerRecognizerTest.java and AlignmentClientIntegrationTest.java to verify 'dg' and 'tth' script types.
6. Run Maven test suite and Pytest test suite.
7. Rebuild local Docker test image (cherokee-aligner-backend:test) and verify container tests pass.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Verified backend test suite (8 passed), ELAN plugin test suite (10 passed), and containerized Docker test run (8 passed).
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Implemented DG, Latin, and TTH script_type support in the backend model runner and ELAN frontend plugin with full CMDI convoc descriptors and unit tests. Verified with Maven unit tests, Pytest, and containerized Docker tests.
<!-- SECTION:FINAL_SUMMARY:END -->
