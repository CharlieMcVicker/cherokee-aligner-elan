---
id: TASK-6
title: Implement ELAN Recognizer Extension & Descriptor
status: Done
assignee:
  - '@myself'
created_date: '2026-08-26 21:11'
updated_date: '2026-08-26 21:12'
labels: []
dependencies: []
ordinal: 6000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Package the Cherokee forced-aligner extension with the required ELAN LocalRecognizer class and CMDI metadata descriptor so ELAN discovers and loads it in the extensions directory.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Implement CherokeeRecognizer extending ELAN LocalRecognizer or Recognizer API
- [x] #2 Provide recognizer.cmdi metadata descriptor in the plugin package
- [x] #3 Integrate with TierAlignmentService and AlignmentOptionsDialog
- [x] #4 Build and deploy updated JAR to ELAN extensions directory
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Implement CherokeeAlignerRecognizer implementing ELAN Recognizer API with TierAlignmentService / AlignmentClient integration.\n2. Create cherokee-aligner.cmdi metadata descriptor.\n3. Add unit tests for CherokeeAlignerRecognizer.\n4. Build shaded uber JAR with Maven.\n5. Install extension directory and JAR into ELAN extensions folder.\n6. Run test suite to verify build.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Implemented CherokeeAlignerRecognizer and packaged with CMDI metadata descriptor; verified with unit tests and installed into ELAN extensions directory.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Implemented CherokeeAlignerRecognizer conforming to ELAN's direct Recognizer extension architecture, added CMDI metadata descriptors, built shaded uber-JAR, and installed to ELAN extensions directory.
<!-- SECTION:FINAL_SUMMARY:END -->
