# Cherokee Forced-Alignment ELAN Plugin

A forced-alignment integration for [ELAN](https://archive.mpi.nl/tla/elan) that automatically aligns Cherokee audio with transcript text.

This plugin allows annotators in ELAN to select a sentence-level annotation on a source tier, trigger alignment, and automatically generate time-aligned word-level annotations on a target tier.

---

## User & Installation Guide

Follow these steps to install and use the plugin with ELAN.

### Quick Overview
The plugin consists of two components:
1. **ELAN Plugin Extension**: Runs inside ELAN to provide the recognizer interface.
2. **Backend Alignment Service**: Runs locally in a Docker container with the PyTorch / HuggingFace alignment model.

---

### Step 1: Download the Plugin Extension

1. Navigate to the **[Releases Page](https://github.com/CharlieMcVicker/cherokee-aligner-elan/releases)** on GitHub.
2. Under the latest release, download the `.zip` file corresponding to your ELAN version:
   * **For ELAN 6.x:** Download `cherokee-aligner-elan-6.zip`
   * **For ELAN 7.x:** Download `cherokee-aligner-elan-7.zip`

---

### Step 2: Install into ELAN

ELAN loads recognizer extensions from its application bundle's `extensions` folder:
`/Applications/ELAN_<version>.app/Contents/app/extensions/cherokee-aligner-ext/`

#### Option A: Manual Extraction (Terminal)
Replace `/Applications/ELAN_6.6.app` and `cherokee-aligner-elan-6.zip` with your version and downloaded file path:

```bash
# Create the extension destination directory
mkdir -p /Applications/ELAN_6.6.app/Contents/app/extensions/cherokee-aligner-ext

# Extract the release zip directly into the extension folder
unzip ~/Downloads/cherokee-aligner-elan-6.zip -d /Applications/ELAN_6.6.app/Contents/app/extensions/cherokee-aligner-ext/
```

#### Option B: Using the Installer Script
If you have cloned this repository or downloaded `scripts/install.sh`:

```bash
./scripts/install.sh /Applications/ELAN_6.6.app ~/Downloads/cherokee-aligner-elan-6.zip
```

---

### Step 3: Run the Backend Alignment Service (Docker)

The alignment model runs inside a pre-built container hosted on GitHub Container Registry (GHCR).

#### Prerequisites:
* [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running.
* **Memory Allocation:** Ensure Docker Desktop has at least **4 GB to 6 GB** of memory allocated (*Docker Desktop Settings > Resources > Memory*).

#### Start the Container:
Run the following command in your terminal to start the background service:

```bash
docker run -d -p 5050:5050 --name cherokee-aligner ghcr.io/charliemcvicker/cherokee-aligner-backend:latest
```

* **Container Image:** `ghcr.io/charliemcvicker/cherokee-aligner-backend:latest`
* **Local Port:** `5050` (`http://localhost:5050`)

To stop or restart the service later:
```bash
docker stop cherokee-aligner     # Stop the service
docker start cherokee-aligner    # Resume running
```

---

### Step 4: Using the Plugin in ELAN

1. **Open ELAN** and load your `.eaf` transcription file and associated audio.
   * *Note: Audio must be standard uncompressed 16-bit PCM `.wav`.*
2. Switch to the **Recognizer** tab in the ELAN main window.
3. In the **Recognizer** dropdown, select **Cherokee Forced Aligner**.
4. Verify the **Server URL** is set to `http://localhost:5050`.
5. Select your **Input Tier** (tier containing sentence annotations) and **Output Tier** (tier where word annotations will be generated).
6. Select your annotation selection / range and click **Start**. Word-level annotations with aligned start and end times will appear on your target tier.

---

### User Troubleshooting & Tips

* **Permission Denied when extracting to `/Applications`:**
  If macOS prevents writing files into `/Applications/ELAN_*.app`, adjust bundle ownership:
  ```bash
  sudo chown -R $(whoami) /Applications/ELAN_*.app
  ```
* **App Management Permissions (macOS Ventura, Sonoma, Sequoia):**
  If terminal commands cannot modify apps in `/Applications`, ensure your terminal has **App Management** enabled under **System Settings > Privacy & Security > App Management**.
* **Audio Format:** ELAN's Java audio extraction requires standard **16-bit PCM WAV** audio files. If your recording is in MP3 or AAC format, convert it to `.wav` before annotating in ELAN.
* **Backend Connection Error:** Check that Docker Desktop is running and verify the container status via `docker ps`.

---

## Developer Guide

The section below contains instructions for building, testing, and developing the plugin and backend from source.

### Repository Layout

```
cherokee-aligner-elan/
├── backend/                  # Python/Flask forced-alignment microservice
│   ├── app.py                # REST API entry point
│   ├── Dockerfile            # Container definition (GHCR image)
│   ├── pyproject.toml        # Python project metadata and dependencies
│   ├── src/                  # Alignment engine, model loader, orthography
│   └── tests/                # Pytest test suite
├── elan-plugin/              # Java ELAN plugin extension
│   ├── lib/                  # Local ELAN jar reference (elan.jar)
│   ├── pom.xml               # Maven configuration & shade plugin
│   └── src/                  # Recognizer extension source & JUnit tests
├── scripts/                  # Build and installation helper scripts
├── test-data/                # Sample .eaf and test fixtures
└── .vscode/                  # VS Code launch & task configurations
```

---

### Developer Prerequisites

* **Python 3.12+** (`brew install python@3.12`)
* **FFmpeg** (`brew install ffmpeg`)
* **Java JDK 17+** (for ELAN 6) or **JDK 21+** (for ELAN 7)
* **Apache Maven 3.9+** (`brew install maven`)
* **ELAN (6.x or 7.x)** installed locally (e.g. `/Applications/ELAN_6.6.app`)
* **Docker** (optional, for local image builds)

---

### Local Backend Development (Python)

1. Create a virtual environment and install dependencies in editable mode:
   ```bash
   cd backend
   python3.12 -m venv venv
   source venv/bin/activate
   pip install -e .
   ```

2. Run backend tests:
   ```bash
   python3 -m pytest tests/
   ```

3. Run the development server:
   ```bash
   python app.py
   ```
   *(Starts on `http://localhost:5050`)*

4. Build and test the Docker container locally:
   ```bash
   docker build -t cherokee-aligner-backend backend/
   docker run -p 5050:5050 cherokee-aligner-backend
   ```

---

### Compiling and Installing the Plugin from Source

#### Build Only:
Compile Java source code and create distributable zips in `dist/`:
```bash
./scripts/build.sh --elan 6   # Build for ELAN 6 (uses Java 17)
./scripts/build.sh --elan 7   # Build for ELAN 7 (uses Java 21)
./scripts/build.sh --all      # Build for both ELAN 6 and 7
```

#### Build and Install in One Step:
Builds the shaded uber JAR, copies CMDI descriptors, and installs directly into your local ELAN installation:
```bash
./scripts/build-and-install.sh
```

---

### Backend API Specification

#### `POST /v1/align/segment`
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
