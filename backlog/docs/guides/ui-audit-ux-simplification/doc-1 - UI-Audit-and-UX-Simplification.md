---
id: doc-1
title: UI Audit and UX Simplification
type: other
created_date: '2026-08-28 14:45'
updated_date: '2026-08-28 14:45'
---
# UI Audit & UX Simplification Specification

## 1. Executive Summary

This document provides a comprehensive audit of the UI elements currently exposed by the Cherokee Forced-Alignment ELAN extension, identifies elements that diverge from the core workflow, and outlines the precise changes to streamline the user experience to the single validated end-to-end path.

### Core Workflow Definition
1. User has an ELAN document open with:
   - A **Source / Input Tier** containing transcribed sentences (e.g. `sentences`).
   - A pre-existing **Target / Output Tier** to receive aligned words (e.g. `words`).
2. User navigates to the **Recognizers** tab in ELAN.
3. User selects the **Cherokee Forced Aligner** recognizer.
4. User specifies the **Source Sentence Tier** and the **Target Word Tier**.
5. User clicks **Start**.
6. The recognizer extracts all segments across the chosen input tier, slices the audio, calls the alignment backend, and populates the word annotations directly into the chosen target words tier.

---

## 2. Current UI Elements Audit

| UI Component | Current Location / Implementation | Current Behavior / Purpose | Keep or Remove? | Rationale |
| :--- | :--- | :--- | :--- | :--- |
| **Server URL Input** | `CherokeeAlignerPanel` (Connection Settings) | `JTextField` with default `http://localhost:5050` | **Keep** | Required so users or developers can point to Docker container or remote backend if needed. |
| **Input Selection Panel (`selectionPanel`)** | `CherokeeAlignerPanel` via ELAN `host.getSelectionPanel()` | Renders ELAN's built-in tabbed / radio UI allowing "Selection" mode vs "Tier" mode | **Strip Selection Mode / Enforce Tier Selection** | Selection mode is broken/unsupported (`TASK-8`). The recognizer descriptor currently allows selection modes which confuses users. |
| **Auto-create Target Tier Option (`[Auto-create: words]`)** | `CherokeeAlignerPanel` (`targetTierCombo`) | Synthetic tier item injected into dropdown at index 0 | **Remove** | Core workflow requires the user to pick an *already existing* word tier in their `.eaf` file. Auto-creation logic creates unlinked/misconfigured tiers and complicates error handling. |
| **Free-text Custom Tier Entry** | `CherokeeAlignerPanel` (`setTargetTierName`) | Allows appending ad-hoc tier names if not in existing tiers | **Remove** | Prevents typo mistakes; target tier dropdown should strictly show existing tiers present in the opened ELAN transcription. |
| **Script Type Selector** | `CherokeeAlignerPanel` (Alignment Settings) | `JComboBox` (`syllabary` vs `latin`) | **Keep** | Essential for Cherokee language orthography selection (Cherokee syllabary vs romanized Latin orthography). |
| **Target Words Tier Dropdown** | `CherokeeAlignerPanel` (Alignment Settings) | `JComboBox` populated with ELAN tiers | **Keep (Streamline)** | Core requirement: user picks which existing tier receives word alignments. |
| **Legacy `AlignmentOptionsDialog`** | `org.cherokee.elan.ui.AlignmentOptionsDialog` | Standalone modal dialog with OK/Cancel buttons | **Remove / Deprecate** | Dead code leftover from earlier menu action experiments; the entire workflow runs inside the ELAN Recognizers tab panel. |
| **`tier` Parameter File XML Fallback** | `CherokeeAlignerRecognizer` | Fallback parser for external XML/CSV files | **Keep (silent background logic)** | Used by ELAN internally when serializing the selected tier to the recognizer. |

---

## 3. Proposed User Experience Changes

### 3.1 recognizer.cmdi Descriptor Updates
- Modify the `<input type="tier">` parameter to strictly enforce tier selection and disallow raw time-selection mode from ELAN's host chooser.
- Clean descriptor metadata so ELAN's recognizer tab does not expose irrelevant input modes or optional unneeded parameters.

### 3.2 `CherokeeAlignerPanel` Layout & Component Simplification
- **Source Tier Selection**:
  - Restrict the input tier chooser strictly to tier selection mode.
- **Target Tier Selection**:
  - `targetTierCombo` will be strictly populated with the valid existing tiers in the active transcription.
  - Remove `[Auto-create: words]` item.
  - Set default selection to `words` if a tier named `words` exists, or the first available tier. If no target tier exists, display an alert/prompt informing the user to create or select an existing target tier.
- **Remove Extraneous Elements**:
  - Remove any legacy or auxiliary buttons/options not relevant to "Select Input Tier -> Select Output Tier -> Run".

### 3.3 Visual Layout Diagram

```
+-------------------------------------------------------------------+
| Cherokee Forced Aligner                                           |
+-------------------------------------------------------------------+
| [ Connection Settings ]                                           |
|   Server URL: [ http://localhost:5050                           ] |
+-------------------------------------------------------------------+
| [ Input & Output Tiers ]                                          |
|   Source Sentence Tier: [ sentences                [v] ]         |
|   Target Words Tier:    [ words                    [v] ]         |
|   Script Type:          [ syllabary                [v] ]         |
+-------------------------------------------------------------------+
| (ELAN Native Controls: [ Start ] [ Stop ] [ Report... ])          |
+-------------------------------------------------------------------+
```

---

## 4. Implementation Steps for Subsequent Tasks

1. **Update `recognizer.cmdi`**:
   - Ensure input constraints only prompt for tier selection.
2. **Refactor `CherokeeAlignerPanel.java`**:
   - Remove `AUTO_CREATE_WORDS` constant and synthetic item insertion.
   - Filter `targetTierCombo` strictly to existing tiers.
   - Simplify parameter validation to check that both source tier and target tier are selected.
3. **Refactor `CherokeeAlignerRecognizer.java`**:
   - Eliminate raw selection fallback path and target tier auto-creation logic.
   - Ensure alignment directly writes to the selected target tier.
4. **Remove Unused UI Classes**:
   - Clean up `AlignmentOptionsDialog.java`.
5. **Update Unit Tests**:
   - Update tests in `CherokeeAlignerRecognizerTest.java` to reflect tier-only validation and dropdown behavior.
