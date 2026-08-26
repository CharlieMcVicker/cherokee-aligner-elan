import os
import io
import wave
from flask import Flask, request, jsonify

app = Flask(__name__)

def run_alignment_engine(wav_bytes: bytes, transcript: str, script_type: str):
    """
    Adapter hook for the Cherokee forced-alignment engine.
    Returns word timestamps relative to the input audio segment start (0 ms).
    """
    words = transcript.strip().split()
    if not words:
        return []
    
    # Inspect audio chunk duration from WAV headers
    with wave.open(io.BytesIO(wav_bytes), 'rb') as wf:
        frames = wf.getnframes()
        rate = wf.getframerate()
        total_duration_ms = int((frames / float(rate)) * 1000) if rate > 0 else 0

    # Uniform slicing adapter hook (to be hooked into Cherokee alignment model)
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

@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"}), 200

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5050))
    app.run(host="0.0.0.0", port=port)
