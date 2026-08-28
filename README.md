# Cherokee Forced-Alignment ELAN Plugin

A monorepo for the Cherokee forced-alignment tool integration with [ELAN](https://archive.mpi.nl/tla/elan).

This project enables ELAN annotators to select a sentence-level annotation on a source tier, trigger an alignment action, and automatically populate word-level annotations on a designated target tier with aligned time boundaries.

---

## Repository Structure

```
cherokee-aligner-elan/
├── backend/                  # Python/Flask forced-alignment microservice
│   ├── app.py                # REST API adapter (/v1/align/segment)
│   ├── Dockerfile            # Container definition
│   ├── pyproject.toml        # Python project metadata and dependencies
│   └── tests/                # Pytest test suite
├── elan-plugin/              # Java ELAN plugin extension
│   ├── lib/                  # Local ELAN jar reference (elan.jar)
│   ├── pom.xml               # Maven configuration & shade plugin
│   └── src/                  # Java plugin source & JUnit tests
├── test-data/                # Sample .eaf and test fixtures
└── .vscode/                  # VS Code launch & task configurations
```

---

## Prerequisites & System Requirements

### macOS / Linux Tools
- **Python 3.12+** (`brew install python@3.12`)
- **FFmpeg** (`brew install ffmpeg`) – required for audio segment resampling and decoding
- **Java JDK 17+** (`brew install openjdk@17`) – only required if compiling the ELAN plugin from source
- **Apache Maven 3.9+** (`brew install maven`) – only required if compiling from source
- **ELAN (6.x or newer)** installed on your machine (e.g. `/Applications/ELAN_6.6.app`)
- **Docker** (optional, for containerized backend execution)

---

## Quickstart & Setup

### 1. Backend Service (Python)

#### Option A: Local Python Environment
Create a virtual environment with Python 3.12 and install backend dependencies:

```bash
cd backend
python3.12 -m venv venv
source venv/bin/activate
pip install -e .
python3 -m pytest tests/
```

Run the backend server locally:

```bash
python app.py
```
*(Runs on `http://localhost:5050` by default)*

#### Option B: Run via Docker

**Using the Pre-Built Release Image (Recommended):**
```bash
docker run -p 5050:5050 ghcr.io/charliemcvicker/cherokee-aligner-backend:latest
```

**Or Build Locally:**
```bash
docker build -t cherokee-aligner-backend backend/
docker run -p 5050:5050 cherokee-aligner-backend
```

---

### 2. ELAN Plugin Installation

ELAN on macOS loads extensions directly from its application package:
`/Applications/ELAN_<version>.app/Contents/app/extensions/cherokee-aligner-ext/`

#### Method 1: Install from GitHub Release Zip (Recommended)
Download the release zip matching your ELAN major version from GitHub Releases:
* **ELAN 6.x:** `cherokee-aligner-elan-6.zip`
* **ELAN 7.x:** `cherokee-aligner-elan-7.zip`

You can extract the zip directly into your ELAN extensions directory:
```bash
mkdir -p /Applications/ELAN_6.6.app/Contents/app/extensions/cherokee-aligner-ext
unzip cherokee-aligner-elan-6.zip -d /Applications/ELAN_6.6.app/Contents/app/extensions/cherokee-aligner-ext/
```

Or pass the zip directly to `install.sh`:
```bash
./scripts/install.sh /Applications/ELAN_6.6.app path/to/cherokee-aligner-elan-6.zip
```

#### Method 2: Install Pre-Built Plugin via Script
If you have already built or downloaded the plugin artifacts in the repository:

```bash
./scripts/install.sh
```

#### Method 3: Build from Source Only (No install)
To compile the plugin and generate the release zip and JAR in `dist/`:

```bash
./scripts/build.sh --elan 6   # Build for ELAN 6
./scripts/build.sh --elan 7   # Build for ELAN 7
./scripts/build.sh --all      # Build for both ELAN 6 & 7
```

#### Method 4: Build from Source & Install in One Step
If you have JDK 17+ (or JDK 21 for ELAN 7) and Maven installed:

```bash
./scripts/build-and-install.sh
```
*(This automatically links `elan.jar`, compiles the shaded uber JAR with Maven via `build.sh`, and installs the plugin and `.cmdi` descriptors into your ELAN application bundle via `install.sh`)*

---

## macOS Troubleshooting & Notes

* **Permission Denied / Ownership Issues:**
  If copying files into `/Applications/ELAN_*.app` fails with permission errors, it is usually because ELAN was installed by a different user account or was previously modified using `sudo`. Fix ownership of the app bundle:
  ```bash
  sudo chown -R $(whoami) /Applications/ELAN_*.app
  ```
  *(Avoid running the install scripts with `sudo` directly so that file ownership remains with your standard user account.)*

* **App Management Permissions (macOS Ventura/Sonoma/Sequoia):**
  If macOS restricts terminal scripts from modifying files inside `/Applications`, ensure your terminal application (Terminal, iTerm2, VS Code) has **App Management** permission enabled in **System Settings > Privacy & Security > App Management**.

* **Supported Audio Formats in ELAN:**
  ELAN's Java audio engine natively slices uncompressed **16-bit PCM WAV** audio files. When creating or annotating transcripts in ELAN, ensure your media files are standard WAV files or have linked WAV audio.

* **Docker Desktop Memory on macOS:**
  The Cherokee alignment model uses PyTorch and HuggingFace transformer models. When running via Docker Desktop on macOS, ensure Docker has at least **4 GB to 6 GB** of allocated memory in Docker settings to avoid container out-of-memory (OOM) errors.

---

## Protocol Specification

### `POST /v1/align/segment`
* **Content-Type:** `multipart/form-data`
* **Form Fields:**
  * `audio`: `[Binary WAV File]` (16kHz, 16-bit, mono)
  * `transcript`: `[String]` (Sentence transcript text in UTF-8)
  * `script_type`: `[String]` (`"syllabary"` | `"latin"`)
* **Response Format:**
```json
{
  "status": "success",
  "script_type": "syllabary",
  "words": [
    { "text": "ᎣᏏᏲ", "start_ms": 0, "end_ms": 450, "confidence": 0.94 },
    { "text": "ᏏᏲ", "start_ms": 480, "end_ms": 820, "confidence": 0.91 }
  ]
}
```

---

## License
MIT
