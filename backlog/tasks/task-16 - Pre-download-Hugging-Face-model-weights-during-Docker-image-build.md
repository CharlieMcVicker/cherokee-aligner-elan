---
id: TASK-16
title: Pre-download Hugging Face model weights during Docker image build
status: Done
assignee:
  - '@antigravity'
created_date: '2026-08-27 16:08'
updated_date: '2026-08-27 16:09'
labels: []
dependencies: []
ordinal: 16000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Create a model pre-download script (download_model.py) and execute it during Dockerfile image build so the container ships with cached model weights, eliminating first-request latency and runtime network dependencies.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 backend/download_model.py invokes get_best_model to cache model and processor artifacts
- [x] #2 backend/Dockerfile executes download_model.py during image build
- [x] #3 backend/download_model.py runs successfully in the backend environment
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Create backend/download_model.py to invoke get_best_model().
2. Update backend/Dockerfile to run python download_model.py during build step.
3. Test backend/download_model.py using backend/venv/bin/python.
4. Verify backend test suite.
<!-- SECTION:PLAN:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Created backend/download_model.py to preload and cache Hugging Face model weights and tokenizer/processor. Added build step to backend/Dockerfile to execute download_model.py, ensuring the container image ships fully offline-ready with zero initial inference download latency. Verified execution with backend/venv/bin/python.
<!-- SECTION:FINAL_SUMMARY:END -->
