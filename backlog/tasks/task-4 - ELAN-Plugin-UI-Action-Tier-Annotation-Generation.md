---
id: TASK-4
title: ELAN Plugin UI Action & Tier Annotation Generation
status: Done
assignee:
  - '@myself'
created_date: '2026-08-26 20:46'
updated_date: '2026-08-26 20:51'
labels: []
dependencies: []
ordinal: 4000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Implement ELAN menu action, Tier / Script Type selection dialog, interval collision detection / overwrite prompt, and AlignableAnnotation population on the target tier.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Plugin extension entry point and menu action registered
- [x] #2 Dialog for tier selection and collision handling implemented
- [x] #3 Target tier annotations created with computed absolute timestamps
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Inspect ELAN extensions API in elan-6.6.jar (Annotation, Tier, Transcription, Recognizer/Extension interfaces).\n2. Implement CherokeeAlignmentEngine/Plugin and UI dialog for selecting target tier and script type.\n3. Implement collision detection and overwrite logic.\n4. Create AlignableAnnotations on target tier with offset t_abs = t_start + word.start_ms.
<!-- SECTION:PLAN:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Implemented TierAlignmentService for interval collision checking, overwrite/abort logic, and absolute timestamp calculation on target tiers, and created AlignmentOptionsDialog.
<!-- SECTION:FINAL_SUMMARY:END -->
