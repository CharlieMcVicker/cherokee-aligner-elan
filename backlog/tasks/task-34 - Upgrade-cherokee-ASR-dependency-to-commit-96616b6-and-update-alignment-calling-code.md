---
id: TASK-34
title: >-
  Upgrade cherokee-ASR dependency to commit 96616b6 and update alignment calling
  code
status: Done
assignee:
  - '@agent'
created_date: '2026-08-30 23:11'
updated_date: '2026-08-30 23:26'
labels: []
dependencies: []
ordinal: 34000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Update transcription service to dev build commit 96616b6d558cdc454ff4c4520a7c012814d2e36e on cherokee-ASR, updating calling code in model_runner, download_model, and test suite to use CherokeeASRModel and ASREmissionsExtractor.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Docker build and pyproject reference commit 96616b6d558cdc454ff4c4520a7c012814d2e36e
- [x] #2 download_model and model_runner use CherokeeASRModel and CherokeeASRExtractor
- [x] #3 Backend test suite passes inside docker image
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Update backend/Dockerfile and backend/pyproject.toml with commit 96616b6d558cdc454ff4c4520a7c012814d2e36e.
2. Update backend/src/download_model.py to use CherokeeASRModel.get_best_model().
3. Update backend/src/model_runner.py to instantiate CherokeeASRModel and CherokeeASRExtractor and pass extractor to align_audio_segment.
4. Update backend/tests/test_aligner.py mocks to reflect new CherokeeASRModel / extractor interfaces.
5. Build the Docker image and run pytest inside the container to verify all tests pass.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Built docker image cherokee-aligner-backend:test and verified all 6 unit/integration tests pass inside container with pytest.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Upgraded cherokee-ASR dependency to dev build commit 96616b6d558cdc454ff4c4520a7c012814d2e36e in Dockerfile and pyproject.toml. Migrated calling code in download_model.py, model_runner.py, and tests to use CherokeeASRModel and CherokeeASRExtractor. Successfully verified with pytest (6 passed) inside the newly built Docker container.
<!-- SECTION:FINAL_SUMMARY:END -->
