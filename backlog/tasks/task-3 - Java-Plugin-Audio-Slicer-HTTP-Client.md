---
id: TASK-3
title: Java Plugin Audio Slicer & HTTP Client
status: Done
assignee:
  - '@myself'
created_date: '2026-08-26 20:46'
updated_date: '2026-08-26 20:49'
labels: []
dependencies: []
ordinal: 3000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Implement AudioSlicer to extract and normalize audio slices to 16kHz mono 16-bit PCM WAV, and AlignmentClient to communicate with the Python backend via multipart/form-data POST.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 AudioSlicer implementation and unit tests
- [x] #2 AlignmentClient implementation with Gson response parsing
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Implement org.cherokee.elan.audio.AudioSlicer to extract slice, resample/normalize to 16kHz 16-bit Mono PCM WAV bytes.\n2. Implement org.cherokee.elan.client.AlignmentClient and data models (AlignmentRequest, AlignmentResponse, WordAlignment) using Apache HttpClient5 and Gson.\n3. Write JUnit tests for AudioSlicer and AlignmentClient.
<!-- SECTION:PLAN:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Implemented AudioSlicer for 16kHz 16-bit mono WAV normalization and slicing, implemented AlignmentClient with Apache HttpClient5 and Gson models, and added passing JUnit tests.
<!-- SECTION:FINAL_SUMMARY:END -->
