"""
Cherokee orthography conversions and transformations.
"""
import logging
from transcription.cherokee.orthography import (
    Orthography,
    clean_punctuation_and_whitespace,
    convert_orthography,
)

logger = logging.getLogger(__name__)


def syllabary_to_phonetics(text: str) -> str:
    """
    Convert Cherokee syllabary text to phonetic representation.
    """
    return convert_orthography(
        text=text,
        source=Orthography.SYLLABARY,
        target=Orthography.TTH,
    )


def phonetics_to_target_script(phonetic_text: str, target_script: str = "syllabary") -> str:
    """
    Convert phonetic text into target script.
    """
    target_ortho = Orthography.SYLLABARY if target_script == "syllabary" else Orthography.TTH
    return convert_orthography(
        text=phonetic_text,
        source=Orthography.TTH,
        target=target_ortho,
    )


def prepare_transcript_for_alignment(transcript: str, script_type: str = "syllabary") -> tuple[str, str]:
    """
    Normalize transcript and produce syllabary and phonetic versions for alignment.

    Returns:
        tuple[str, str]: (syllabary_text, raw_phonetic)
    """
    if script_type == "syllabary":
        syllabary_text = transcript.strip()
        raw_phonetic = convert_orthography(
            text=syllabary_text,
            source=Orthography.SYLLABARY,
            target=Orthography.TTH,
        )
    else:
        syllabary_text = ""
        raw_phonetic = clean_punctuation_and_whitespace(transcript)

    return syllabary_text, raw_phonetic

