"""
Model runner and alignment engine execution.
"""
import io
import logging
from pydub import AudioSegment
from transcription.alignment.core.sliding_window import SlidingWindowDTWAligner
from transcription.alignment.domain.models import TextChunk
from transcription.alignment.strategies.extractors import CherokeeASRExtractor
from transcription.alignment.strategies.reconciliation import CherokeeSyllabaryReconciliationStrategy
from transcription.models.asr_model import CherokeeASRModel
from orthography import prepare_transcript_for_alignment

logger = logging.getLogger(__name__)

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

    # 2. Build ground-truth chunk via orthography helper
    syllabary_text, raw_phonetic = prepare_transcript_for_alignment(transcript, script_type)

    chunks = [
        TextChunk(
            chunk_id="seg_0",
            raw_text=raw_phonetic,
            syllabary_text=syllabary_text if syllabary_text else None,
        )
    ]

    # 3. Perform CTC emissions extraction & DTW alignment
    asr_model = CherokeeASRModel.get_best_model()
    extractor = CherokeeASRExtractor(model=asr_model, skip_vad=True)
    emissions = extractor.extract(audio_seg)

    reconciliation_strategy = (
        CherokeeSyllabaryReconciliationStrategy() if script_type == "syllabary" else None
    )
    aligner = SlidingWindowDTWAligner(reconciliation_strategy=reconciliation_strategy)
    alignment = aligner.align_chunks(emissions=emissions, chunks=chunks)

    results = []
    if alignment.aligned_chunks and alignment.aligned_chunks[0].words:
        for w in alignment.aligned_chunks[0].words:
            # Prefer original syllabary text if available for the word, or fallback to word
            text = w.syllabary if (script_type == "syllabary" and w.syllabary) else w.word
            start_ms = max(0, int(round(w.start_sec * 1000)))
            end_ms = min(total_duration_ms, int(round(w.end_sec * 1000)))
            if end_ms <= start_ms:
                # If zero duration, give at least a minimal interval
                end_ms = min(total_duration_ms, start_ms + int(total_duration_ms / len(words)))
            results.append({
                "text": text,
                "start_ms": start_ms,
                "end_ms": end_ms,
                "confidence": round(float(w.confidence), 4)
            })

    # Fallback to uniform division if alignment produced no words (e.g. silent audio / unaligned)
    if not results:
        step = total_duration_ms / max(len(words), 1)
        for i, w in enumerate(words):
            results.append({
                "text": w,
                "start_ms": int(i * step),
                "end_ms": int((i + 1) * step),
                "confidence": 0.5
            })

    return results
