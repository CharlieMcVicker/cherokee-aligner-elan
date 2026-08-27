---
id: TASK-15
title: >-
  Create GitHub Actions CI/CD release workflow for plugin JAR and GHCR Docker
  image
status: Done
assignee:
  - '@antigravity'
created_date: '2026-08-27 16:07'
updated_date: '2026-08-27 16:07'
labels: []
dependencies: []
ordinal: 15000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Implement a GitHub Actions workflow (.github/workflows/release.yml) triggered on git tag pushes (v*). It will automatically compile the ELAN plugin JAR, publish a GitHub Release with assets attached, and build and publish the backend Docker image to GitHub Container Registry (ghcr.io).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 .github/workflows/release.yml is created and configured to run on tag push (v*)
- [x] #2 Plugin build job sets up Java, resolves elan.jar in CI, compiles shaded uber JAR, and publishes release assets
- [x] #3 Docker job builds backend image and pushes to ghcr.io using GITHUB_TOKEN
- [x] #4 README.md documents using pre-built Docker images from ghcr.io
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Create .github/workflows/release.yml with release-plugin and release-docker jobs.
2. Configure permissions for contents: write and packages: write.
3. In release-plugin job: fetch ELAN linux archive to link elan.jar, run mvn package, and upload assets to gh release using softprops/action-gh-release or gh CLI.
4. In release-docker job: use docker/build-push-action to build backend/Dockerfile and push to ghcr.io with latest and tag versions.
5. Update README.md with instructions for running the pre-built ghcr.io Docker container.
<!-- SECTION:PLAN:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Implemented .github/workflows/release.yml to automate plugin JAR compilation (with automated ELAN library download and softprops/action-gh-release asset publishing) and backend Docker container publishing to GitHub Container Registry (ghcr.io) on git tag push (v*). Updated README.md with instructions to run pre-built container images directly from ghcr.io.
<!-- SECTION:FINAL_SUMMARY:END -->
