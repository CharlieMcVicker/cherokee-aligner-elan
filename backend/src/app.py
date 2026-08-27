import os
import logging
from flask import Flask, request, jsonify
from model_runner import run_alignment

# Configure logging
logger = logging.getLogger(__name__)
logging.basicConfig(level=logging.INFO)

app = Flask(__name__)

@app.route("/v1/align/segment", methods=["POST"])
def align_segment():
    if "audio" not in request.files or "transcript" not in request.form:
        return jsonify({"error": "Missing 'audio' file or 'transcript' text"}), 400

    audio_file = request.files["audio"]
    transcript = request.form["transcript"]
    script_type = request.form.get("script_type", "syllabary")

    try:
        wav_bytes = audio_file.read()
        word_alignments = run_alignment(wav_bytes, transcript, script_type)
        return jsonify({
            "status": "success",
            "script_type": script_type,
            "words": word_alignments
        }), 200
    except Exception as e:
        logger.exception("Error during alignment")
        return jsonify({"error": str(e)}), 500

@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"}), 200

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5050))
    app.run(host="0.0.0.0", port=port)
