"""
Model runner and alignment engine execution.
"""
import io
import logging
from typing import Optional
from pydub import AudioSegment

from transcription.alignment import CTCSegmentationAligner, CTCAlignerConfig, TextChunk
from transcription.cherokee.models import CherokeeASRModel
from orthography import prepare_transcript_for_alignment

logger = logging.getLogger(__name__)

_model: Optional[CherokeeASRModel] = None


def get_aligner_model() -> CherokeeASRModel:
    """Retrieve or lazily initialize the Cherokee ASR model."""
    global _model
    if _model is None:
        _model = CherokeeASRModel.get_best_model()
    return _model


def normalize_audio_to_16k(wav_bytes: bytes) -> AudioSegment:
    """Normalize input audio to 16kHz, mono 16-bit PCM AudioSegment."""
    seg = AudioSegment.from_file(io.BytesIO(wav_bytes))
    if seg.frame_rate != 16000 or seg.channels != 1:
        seg = seg.set_frame_rate(16000).set_channels(1)
    return seg


def run_alignment(wav_bytes: bytes, transcript: str, script_type: str = "syllabary"):
    """
    Executes forced alignment for a single audio segment and transcript.
    Returns word timestamps relative to the input audio segment start (0 ms).
    """
    words = transcript.strip().split()
    if not words:
        return []

    # 1. Normalize audio to 16kHz mono
    audio_seg = normalize_audio_to_16k(wav_bytes)
    total_duration_ms = len(audio_seg)

    # 2. Build text chunk via orthography helper
    syllabary_text, raw_phonetic = prepare_transcript_for_alignment(transcript, script_type)
    text_content = raw_phonetic if raw_phonetic else transcript
    chunks = [TextChunk(chunk_id="seg_0", text=text_content)]

    # 3. Perform CTC Segmentation Alignment
    model = get_aligner_model()
    config = CTCAlignerConfig()
    aligner = CTCSegmentationAligner(model=model, config=config)
    output = aligner.align(audio_input=audio_seg, chunks=chunks)

    results = []
    if output.aligned_chunks and output.aligned_chunks[0].words:
        aligned_words = output.aligned_chunks[0].words
        for i, w in enumerate(aligned_words):
            text = words[i] if (script_type == "syllabary" and i < len(words)) else w.word
            start_ms = max(0, int(round(w.start_sec * 1000)))
            end_ms = min(total_duration_ms, int(round(w.end_sec * 1000)))
            if end_ms <= start_ms:
                end_ms = min(total_duration_ms, start_ms + int(total_duration_ms / len(aligned_words)))
            results.append({
                "text": text,
                "start_ms": start_ms,
                "end_ms": end_ms,
                "confidence": round(float(w.confidence), 4),
            })

    # Fallback to uniform division if alignment produced no words (e.g. silent audio / unaligned)
    if not results:
        step = total_duration_ms / max(len(words), 1)
        for i, w in enumerate(words):
            results.append({
                "text": w,
                "start_ms": int(i * step),
                "end_ms": int((i + 1) * step),
                "confidence": 0.5,
            })

    return results

