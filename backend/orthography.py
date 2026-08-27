"""
Cherokee orthography conversions and transformations.
"""
import logging
from transcription.utils.syllabary_map import cherokee_to_bad_phonetics

logger = logging.getLogger(__name__)

def syllabary_to_phonetics(text: str) -> str:
    """
    Convert Cherokee syllabary text to phonetic representation.
    Currently wraps cherokee_to_bad_phonetics.
    """
    return cherokee_to_bad_phonetics(text)

def phonetics_to_target_script(phonetic_text: str, target_script: str = "syllabary") -> str:
    """
    Convert phonetic text into another writing system using library functions.
    Placeholder / extensible hook for upcoming orthography conversions.
    """
    # Note: Will integrate library conversion function when configured
    return phonetic_text

def prepare_transcript_for_alignment(transcript: str, script_type: str):
    """
    Normalize transcript and produce syllabary and phonetic versions for alignment verses.
    
    Returns:
        tuple[str, str]: (syllabary_text, raw_phonetic)
    """
    if script_type == "syllabary":
        syllabary_text = transcript
        raw_phonetic = syllabary_to_phonetics(transcript)
    else:
        syllabary_text = ""
        raw_phonetic = transcript

    return syllabary_text, raw_phonetic
