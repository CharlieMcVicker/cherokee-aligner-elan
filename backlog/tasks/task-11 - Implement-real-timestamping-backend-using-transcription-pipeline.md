---
id: TASK-11
title: Implement real timestamping backend using transcription pipeline
status: Done
assignee:
  - '@agent'
created_date: '2026-08-27 14:26'
updated_date: '2026-08-27 14:42'
labels: []
dependencies: []
ordinal: 11000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Replace mock alignment in backend/app.py with real Cherokee wav2vec2 alignment using align_audio_segment from transcription.timestamping.aligner and model loading utilities.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Backend loads wav2vec2 model and processor based on model config
- [x] #2 Backend invokes align_audio_segment on input audio WAV and transcript
- [x] #3 Returns word-level timestamp intervals and confidence scores in alignment response
- [x] #4 Existing and new backend tests pass
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Research the integration points in backend/app.py, transcription.timestamping.aligner.align_audio_segment, and transcription.utils.model_utils.get_best_model_config.
2. Implement model/processor loader in backend/app.py (lazy-loaded or startup loaded) using Wav2Vec2ForCTC/Wav2Vec2Processor or load_asr_model, falling back gracefully if needed.
3. Update run_alignment_engine in backend/app.py to convert input audio bytes (via io.BytesIO or AudioSegment) and input transcript into the verses structure required by align_audio_segment.
4. Call align_audio_segment with skip_vad=True (since ELAN sends pre-sliced audio segments) and extract word-level timestamps (converting start_sec/end_sec to start_ms/end_ms).
5. Update/expand backend pytest test suite in backend/tests/test_aligner.py to test real alignment with mock/fixture and real align_audio_segment calls.
6. Verify all unit tests pass with pytest.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Updated model loading in backend/app.py to use get_best_model_config from transcription.utils.model_utils and load Wav2Vec2ForCTC/Wav2Vec2Processor directly, resolving the import issue in Docker container environments.

Reinstalled workshop-transcription in the local virtual environment at commit 44a503a544ef7338981ab0d75235005d9d1db943. Verified get_best_model is exported from transcription.utils.model_utils and all pytest tests pass.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Implemented real Wav2Vec2 Cherokee forced-alignment backend in backend/app.py:
- Added normalize_audio_to_16k to resample and downmix input audio bytes to 16kHz mono.
- Integrated load_asr_model to load and cache the best Wav2Vec2 model and processor on the active device.
- Integrated cherokee_to_bad_phonetics and align_audio_segment with skip_vad=True for pre-sliced ELAN segments.
- Added uniform slicing fallback for unaligned / silent segments.
- Updated backend test suite in backend/tests/test_aligner.py, verified all 5 tests pass.
<!-- SECTION:FINAL_SUMMARY:END -->
