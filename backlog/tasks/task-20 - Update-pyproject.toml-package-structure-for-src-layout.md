---
id: TASK-20
title: Update pyproject.toml package structure for src/ layout
status: Done
assignee:
  - '@antigravity'
created_date: '2026-08-27 19:03'
updated_date: '2026-08-27 19:05'
labels: []
dependencies: []
ordinal: 20000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Configure pyproject.toml setuptools/hatchling package discovery for the new src/ directory layout and verify install and build.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 pyproject.toml configures src/ package directory layout
- [x] #2 pip install -e . succeeds in backend virtualenv
- [x] #3 python -m build or pytest succeeds
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Update backend/pyproject.toml:
   - Configure setuptools package finding with [tool.setuptools.packages.find] where = ['src']
   - Update pytest configuration (pythonpath = ['src'])
2. Update backend/Dockerfile to execute app and download_model properly (e.g. PYTHONPATH=/app/src or CMD ['python', 'src/app.py'])
3. Reinstall package in editable mode with ./backend/venv/bin/pip install -e .
4. Run tests with ./backend/venv/bin/pytest backend/tests and verify build with ./backend/venv/bin/python -m build backend
5. Verify and check off acceptance criteria, write final summary, and mark task Done
<!-- SECTION:PLAN:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Configured [tool.setuptools.packages.find] with where = ['src'] and pytest pythonpath in pyproject.toml. Updated Dockerfile to point to src/ modules and PYTHONPATH. Verified editable install (pip install -e .), package build (python -m build --wheel), and unit test execution (pytest tests).
<!-- SECTION:FINAL_SUMMARY:END -->
