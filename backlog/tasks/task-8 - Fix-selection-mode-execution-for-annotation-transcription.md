---
id: TASK-8
title: Fix selection mode execution for annotation transcription
status: To Do
assignee: []
created_date: '2026-08-26 21:54'
labels: []
dependencies: []
ordinal: 8000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
When selecting an annotation in ELAN and running the recognizer in selection mode, nothing happens. Investigate and fix how selection boundaries/annotations are extracted and passed to the alignment/transcription pipeline.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Running the recognizer in selection mode properly transcribes/aligns selected annotations
- [ ] #2 Both full tier mode and selection mode work seamlessly
<!-- AC:END -->
