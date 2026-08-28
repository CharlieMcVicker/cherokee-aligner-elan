---
id: TASK-28
title: 'Unify Docker backend build, test, and publish to deploy tested image'
status: Done
assignee:
  - '@antigravity'
created_date: '2026-08-28 14:11'
updated_date: '2026-08-28 14:11'
labels: []
dependencies: []
ordinal: 28000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Eliminate redundant Docker image build in release workflow by building the image once, verifying it with pytest tests, and publishing the exact tested image to GHCR.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Docker backend image is built once per release workflow run
- [x] #2 Tests are executed against the built image before pushing to GHCR
- [x] #3 The verified image is tagged and pushed directly to GHCR upon test success
- [x] #4 Release job dependencies are updated to reflect the unified backend job
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Replace separate test-backend and publish-docker jobs in .github/workflows/release.yml with a unified backend release job.
2. Build the Docker image once using Docker Buildx and load it locally.
3. Execute pytest tests inside the built and loaded Docker container.
4. Tag and push the verified Docker image directly to GHCR using the extracted metadata tags.
5. Update release-github job dependencies to depend on the unified backend job.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Consolidated test-backend and publish-docker into build-test-publish-backend in .github/workflows/release.yml. Configured buildx to load cherokee-aligner:test into local daemon, run pytest container tests, and push the tested image directly to GHCR upon success.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Unified Docker backend testing and publishing into a single workflow job (build-test-publish-backend) in .github/workflows/release.yml. The image is now built once, loaded locally, tested with pytest, and pushed directly to GHCR upon test pass, eliminating duplicate build times and ensuring the deployed image is identical to the tested image.
<!-- SECTION:FINAL_SUMMARY:END -->
