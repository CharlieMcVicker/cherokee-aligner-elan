Here is the technical specification and foundational starter code for the monorepo.

---

# Architecture & Developer Environment

### 1. Build & Dependency Configuration (`elan-plugin/pom.xml`)

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.cherokee.elan</groupId>
    <artifactId>cherokee-aligner-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Local ELAN binary reference extracted from installed app -->
        <dependency>
            <groupId>nl.mpi.elan</groupId>
            <artifactId>elan</artifactId>
            <version>6.8</version>
            <scope>system</scope>
            <systemPath>${project.basedir}/lib/elan.jar</systemPath>
        </dependency>
        <dependency>
            <groupId>org.apache.httpcomponents.client5</groupId>
            <artifactId>httpclient5</artifactId>
            <version>5.3.1</version>
        </dependency>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.10.1</version>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.5.2</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals><goal>shade</goal></goals>
                        <configuration>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
                            </transformers>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>

```

---

### 2. VS Code Monorepo Automation (`.vscode/launch.json` & `.vscode/tasks.json`)

**`.vscode/launch.json`**

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "name": "Python: Flask Align Server",
      "type": "debugpy",
      "request": "launch",
      "program": "${workspaceFolder}/backend/app.py",
      "env": { "FLASK_ENV": "development", "PORT": "5050" },
      "console": "integratedTerminal"
    },
    {
      "name": "Attach Java: ELAN Debugger (Port 5005)",
      "type": "java",
      "request": "attach",
      "hostName": "localhost",
      "port": 5005
    }
  ]
}

```

**`.vscode/tasks.json`**

```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Build & Install Plugin to ELAN",
      "type": "shell",
      "command": "mvn clean package && cp target/cherokee-aligner-plugin-1.0.0-SNAPSHOT.jar ~/Library/Application\\ Support/ELAN/extensions/",
      "options": { "cwd": "${workspaceFolder}/elan-plugin" },
      "group": { "kind": "build", "isDefault": true }
    },
    {
      "label": "Launch ELAN in Debug Mode (macOS)",
      "type": "shell",
      "command": "/Applications/ELAN.app/Contents/MacOS/ELAN -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005",
      "isBackground": true
    }
  ]
}

```

---

### 3. Backend Implementation (`backend/app.py`)

```python
import os
import io
import wave
from flask import Flask, request, jsonify

app = Flask(__name__)

def run_alignment_engine(wav_bytes: bytes, transcript: str, script_type: str):
    """
    Adapter hook for the existing Cherokee forced-alignment tool.
    Assumes word timestamps relative to the input audio segment start (0 ms).
    """
    words = transcript.strip().split()
    if not words:
        return []
    
    # Inspect audio chunk duration
    with wave.open(io.BytesIO(wav_bytes), 'rb') as wf:
        frames = wf.getnframes()
        rate = wf.getframerate()
        total_duration_ms = int((frames / float(rate)) * 1000)

    # Stub uniform slicing (to be replaced by the tool invocation)
    step = total_duration_ms / max(len(words), 1)
    results = []
    for i, w in enumerate(words):
        results.append({
            "text": w,
            "start_ms": int(i * step),
            "end_ms": int((i + 1) * step),
            "confidence": 0.95
        })
    return results

@app.route("/v1/align/segment", methods=["POST"])
def align_segment():
    if "audio" not in request.files or "transcript" not in request.form:
        return jsonify({"error": "Missing 'audio' file or 'transcript' text"}), 400

    audio_file = request.files["audio"]
    transcript = request.form["transcript"]
    script_type = request.form.get("script_type", "syllabary")

    try:
        wav_bytes = audio_file.read()
        word_alignments = run_alignment_engine(wav_bytes, transcript, script_type)
        return jsonify({
            "status": "success",
            "script_type": script_type,
            "words": word_alignments
        }), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=int(os.environ.get("PORT", 5050)))

```

---

### 4. Backend Containerization (`backend/Dockerfile`)

```dockerfile
FROM python:3.11-slim

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    ffmpeg \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE 5050
CMD ["python", "app.py"]

```

---

### 5. Java Audio Segmenter (`AudioSlicer.java`)

```java
package org.cherokee.elan.audio;

import javax.sound.sampled.*;
import java.io.*;

public class AudioSlicer {

    public static byte[] extractSegmentToWav(File sourceMedia, long startMs, long endMs) throws Exception {
        try (AudioInputStream sourceStream = AudioSystem.getAudioInputStream(sourceMedia)) {
            AudioFormat baseFormat = sourceStream.getFormat();
            
            // Normalize to 16kHz, 16-bit, Mono PCM for alignment pipeline
            AudioFormat targetFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                16000.0f, 16, 1, 2, 16000.0f, false
            );

            AudioInputStream convertedStream = AudioSystem.getAudioInputStream(targetFormat, sourceStream);
            
            long bytesPerMillisecond = (long) (16000.0f * 2 / 1000.0);
            long bytesToSkip = startMs * bytesPerMillisecond;
            long bytesToRead = (endMs - startMs) * bytesPerMillisecond;

            long skipped = convertedStream.skip(bytesToSkip);
            if (skipped < bytesToSkip) {
                throw new IOException("Unable to reach segment offset in audio file");
            }

            byte[] segmentData = convertedStream.readNBytes((int) bytesToRead);
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (AudioInputStream segmentStream = new AudioInputStream(
                    new ByteArrayInputStream(segmentData), targetFormat, segmentData.length / targetFormat.getFrameSize())) {
                AudioSystem.write(segmentStream, AudioFileFormat.Type.WAVE, out);
            }
            return out.toByteArray();
        }
    }
}

```

---

# SPECIFICATION HANDOFF: `PROJECT_SPEC.md`

```markdown
# Project Spec: Cherokee Forced-Alignment ELAN Plugin

## 1. Project Goal
Enable ELAN annotators to select a sentence-level annotation on a source tier, trigger an alignment action, and automatically populate word-level annotations on a designated target tier with aligned time boundaries.

---

## 2. System Architecture
The repository is structured as a monorepo:
* **`/backend`**: Python/Flask REST service running in a Docker container, wrapping the existing Cherokee forced-alignment model.
* **`/elan-plugin`**: Java extension deployed to ELAN's `extensions/` directory as a Fat JAR.

---

## 3. Communication Protocol

### `POST /v1/align/segment`
* **Content-Type:** `multipart/form-data`
* **Form Fields:**
  * `audio`: `[Binary WAV File]` (16kHz, 16-bit, mono)
  * `transcript`: `[String]` (Sentence transcript text)
  * `script_type`: `[String]` (`"syllabary"` | `"latin"`)
* **Response Format (JSON):**
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

*Note: Backend returns timestamps relative to segment start ($t=0$). The Java plugin converts these to absolute ELAN timeline timestamps ($t_{abs} = t_{start} + t_{relative}$).*

---

## 4. Plugin Workflow & UI Execution

```
[1. User selects Annotation on Source Tier]
                     │
                     ▼
[2. Click Menu Action: "Tier" > "Timestamp Cherokee Words"]
                     │
                     ▼
[3. Options Dialog Appears]
   ├─ Target Tier: [Dropdown: Lists all non-source tiers]
   └─ Script Type: [Dropdown: "Cherokee Syllabary" / "Latin"]
                     │
                     ▼
[4. Collision Pre-Check]
   Is there any annotation in Target Tier within [t_start, t_end]?
   ├─ YES ──► Prompt: "Overlap detected. Overwrite or Abort?"
   │            ├─ Abort ────► Exit without changes
   │            └─ Overwrite ─► Remove conflicting annotations in interval
   └─ NO ───► Proceed
                     │
                     ▼
[5. Execution & Extraction]
   ├─ Slice audio from linked media file between [t_start, t_end]
   ├─ Send audio + transcript + script_type to Backend
   ├─ Parse JSON response
   └─ For each word:
        Create AlignableAnnotation(
            tier = targetTier, 
            begin = t_start + word.start_ms, 
            end = t_start + word.end_ms, 
            value = word.text
        )
                     │
                     ▼
[6. ELAN Document Updated & Repainted]

```

---

## 5. Error Handling & Edge Cases

1. **Unlinked Media:** If no audio file is associated with the `.eaf` document, fail gracefully with an alert dialog (*"No audio media associated with this transcription"*).
2. **Empty Transcript:** If the selected annotation has whitespace only, abort without dispatching an HTTP request.
3. **HTTP / Service Failure:** If the container is down or returns 500, present a non-blocking error dialog displaying the server message.
4. **Time Overrun:** Ensure $t_{start} + \text{word.end\_ms} \le t_{end}$ of the source annotation to maintain tier validity.

```

```