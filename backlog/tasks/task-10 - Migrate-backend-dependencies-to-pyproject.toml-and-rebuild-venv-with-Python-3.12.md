---
id: TASK-10
title: >-
  Migrate backend dependencies to pyproject.toml and rebuild venv with Python
  3.12
status: Done
assignee:
  - '@antigravity'
created_date: '2026-08-27 13:56'
updated_date: '2026-08-27 13:59'
labels: []
dependencies: []
ordinal: 10000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Transition backend dependency management from requirements.txt to pyproject.toml, remove requirements.txt, update Dockerfile and documentation accordingly, and rebuild backend venv using Python 3.12.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 pyproject.toml is created in backend/ with all existing dependencies
- [x] #2 requirements.txt is removed from backend/
- [x] #3 backend virtual environment is recreated using Python 3.12
- [x] #4 All dependencies install cleanly into the new venv and pytest passes
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Create backend/pyproject.toml with project metadata, python >=3.12 requirement, and dependencies (Flask, pytest, requests, cherokee-asr).
2. Remove backend/requirements.txt.
3. Update backend/Dockerfile to install from pyproject.toml and use python:3.12-slim.
4. Remove existing backend/venv and recreate using python3.12 -m venv backend/venv.
5. Install dependencies with pip install -e . into backend/venv.
6. Verify backend tests pass with backend/venv/bin/pytest backend/tests.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Replaced backend/requirements.txt with backend/pyproject.toml targeting Python >=3.12 and PEP 508 dependencies. Rebuilt backend/venv using Python 3.12.6. Updated Dockerfile to python:3.12-slim and pyproject installation. Verified with backend/venv/bin/pytest backend/tests (3/3 passing).
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Migrated backend dependency management from requirements.txt to pyproject.toml with requires-python >= 3.12 and PEP 508 dependencies. Recreated backend/venv using Python 3.12.6, updated Dockerfile and README.md, and verified clean installation and passing pytest suite.
<!-- SECTION:FINAL_SUMMARY:END -->
