---
id: TASK-25
title: >-
  Update release workflow with dynamic versioning, caching, and test
  verification
status: Done
assignee:
  - '@antigravity'
created_date: '2026-08-28 13:38'
updated_date: '2026-08-28 13:39'
labels: []
dependencies: []
ordinal: 25000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Update .github/workflows/release.yml to dynamically name release artifacts based on the git tag, add reference JAR caching, and run test verification before publishing assets.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Release workflow dynamically sets plugin artifact version from tag without hardcoded -SNAPSHOT
- [x] #2 Release workflow caches ELAN reference JARs similar to CI workflow
- [x] #3 Release workflow runs test verification prior to publishing release assets
- [x] #4 GitHub release and GHCR publishing steps work with dynamic versioning
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Structure release.yml with matrix build for plugins (Java 17 for ELAN 6.6, Java 21 for ELAN 7.1) matching ci.yml.
2. Ensure each matrix job runs tests, packages the JAR with dynamic release versioning, and uploads the artifact.
3. Add backend test job (test-backend).
4. Create release-github job needing matrix plugin builds and test-backend, downloading artifacts and publishing to GitHub Release.
5. Create publish-docker job needing test-backend, building and publishing to GHCR.
6. Verify workflow syntax and YAML structure.
<!-- SECTION:PLAN:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Refactored .github/workflows/release.yml to use a matrix build strategy compiling ELAN 6.6 with Java 17 and ELAN 7.1 with Java 21. Added reference JAR caching, maven test verification, Docker backend testing prerequisite, dynamic artifact versioning from git tags (without -SNAPSHOT in release assets), and artifact collection for GitHub Releases.
<!-- SECTION:FINAL_SUMMARY:END -->
