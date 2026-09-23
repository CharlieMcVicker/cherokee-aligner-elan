"""
Model runner and alignment engine execution.
"""
import io
import logging
from typing import Optional
from pydub import AudioSegment

from transcription.alignment import CTCSegmentationAligner, CTCAlignerConfig, TextChunk
from transcription.cherokee.models import CherokeeASRModel
from transcription.cherokee.orthography import (
    Orthography,
    clean_punctuation_and_whitespace,
    convert_orthography,
)

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
    source_words = transcript.strip().split()
    if not source_words:
        return []

    # 1. Normalize audio to 16kHz mono
    audio_seg = normalize_audio_to_16k(wav_bytes)
    total_duration_ms = len(audio_seg)

    # 2. Build text chunk and configure phonotactics
    if script_type == "syllabary":
        phonetic_text = convert_orthography(
            text=transcript.strip(),
            source=Orthography.SYLLABARY,
            target=Orthography.TTH,
        )
        enforce_phonotactics = True
    else:
        phonetic_text = clean_punctuation_and_whitespace(transcript)
        enforce_phonotactics = False

    chunks = [TextChunk(chunk_id="seg_0", text=phonetic_text)]

    # 3. Perform CTC Segmentation Alignment
    model = get_aligner_model()
    config = CTCAlignerConfig(enforce_phonotactics=enforce_phonotactics)
    aligner = CTCSegmentationAligner(model=model, config=config)
    output = aligner.align(audio_input=audio_seg, chunks=chunks)

    results = []
    if output.aligned_chunks and output.aligned_chunks[0].words:
        aligned_words = output.aligned_chunks[0].words
        for i, w in enumerate(aligned_words):
            # Index directly into original source words array
            text = source_words[i] if i < len(source_words) else w.word
            start_ms = max(0, int(round(w.start_sec * 1000)))
            end_ms = min(total_duration_ms, int(round(w.end_sec * 1000)))
            if end_ms <= start_ms:
                end_ms = min(
                    total_duration_ms,
                    start_ms + int(total_duration_ms / len(aligned_words)),
                )
            results.append({
                "text": text,
                "start_ms": start_ms,
                "end_ms": end_ms,
                "confidence": round(float(w.confidence), 4),
            })

    # Fallback to uniform division if alignment produced no words (e.g. silent audio / unaligned)
    if not results:
        step = total_duration_ms / max(len(source_words), 1)
        for i, w in enumerate(source_words):
            results.append({
                "text": w,
                "start_ms": int(i * step),
                "end_ms": int((i + 1) * step),
                "confidence": 0.5,
            })

    return results
