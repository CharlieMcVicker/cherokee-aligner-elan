---
id: TASK-37
title: Migrate backend aligner to CTC segmentation and update orthography
status: Done
assignee:
  - '@agent'
created_date: '2026-09-23 18:31'
updated_date: '2026-09-23 18:36'
labels: []
dependencies: []
modified_files:
  - backend/pyproject.toml
  - backend/src/orthography.py
  - backend/src/model_runner.py
  - backend/tests/test_aligner.py
ordinal: 34000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Major updates in cherokee-ASR introduce CTCSegmentationAligner for syncope-aware forced alignment and centralized transcription.cherokee.orthography utilities. We need to refactor backend model_runner and orthography modules to use the new architecture directly without backward compatibility constraints.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Update backend/pyproject.toml dependency to latest cherokee-ASR commit 7693a21
- [x] #2 Refactor backend/src/orthography.py to cleanly delegate to transcription.cherokee.orthography
- [x] #3 Refactor backend/src/model_runner.py to use CTCSegmentationAligner with TextChunk and CTCAlignerConfig
- [x] #4 Update backend/tests/test_aligner.py unit tests to reflect the new pipeline and verify full test pass
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [x] #1 pytest passes cleanly
<!-- DOD:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Update backend/pyproject.toml with git reference to commit 7693a21.\n2. Refactor backend/src/orthography.py to cleanly delegate to transcription.cherokee.orthography (convert_orthography, Orthography, clean_punctuation_and_whitespace).\n3. Refactor backend/src/model_runner.py to use CTCSegmentationAligner, CTCAlignerConfig, and TextChunk.\n4. Update backend/tests/test_aligner.py to test the new CTCSegmentationAligner integration and orthography helpers.\n5. Run pytest and verify all tests pass.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Updated upstream workshop-transcription dependency to commit 7693a21. Refactored orthography.py to cleanly delegate to transcription.cherokee.orthography (Orthography, convert_orthography, clean_punctuation_and_whitespace). Updated model_runner.py to use CTCSegmentationAligner, CTCAlignerConfig, TextChunk, and CherokeeASRModel. Updated unit tests in test_aligner.py and verified all 6 unit tests pass.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Migrated backend aligner to CTCSegmentationAligner and centralized Cherokee orthography conversions. All unit tests verified passing with pytest.
<!-- SECTION:FINAL_SUMMARY:END -->
