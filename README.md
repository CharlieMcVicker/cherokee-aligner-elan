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
│   ├── requirements.txt      # Python dependencies
│   └── tests/                # Pytest test suite
├── elan-plugin/              # Java ELAN plugin extension
│   ├── lib/                  # Local ELAN jar reference (elan.jar)
│   ├── pom.xml               # Maven configuration & shade plugin
│   └── src/                  # Java plugin source & JUnit tests
├── test-data/                # Sample .eaf and test fixtures
└── .vscode/                  # VS Code launch & task configurations
```

---

## Quickstart & Development

### 1. Backend Service (Python)

Create a virtual environment and run the test suite:

```bash
cd backend
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
pytest tests/
```

Run the backend server locally:

```bash
python app.py
```

Or build and run via Docker:

```bash
docker build -t cherokee-aligner-backend backend/
docker run -p 5050:5050 cherokee-aligner-backend
```

### 2. ELAN Plugin (Java)

Prerequisites:
- Java 17+
- Maven 3.9+
- Local ELAN installation (`elan-plugin/lib/elan.jar`)

Build and run tests:

```bash
cd elan-plugin
mvn test
```

Package the shaded uber JAR:

```bash
mvn clean package
```

The output fat JAR will be generated at:
```
elan-plugin/target/cherokee-aligner-plugin-1.0.0-SNAPSHOT.jar
```

Copy this JAR to your local ELAN extensions directory (e.g. `~/Library/Application Support/ELAN/extensions/` on macOS).

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
