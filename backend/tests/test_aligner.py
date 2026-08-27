import io
import wave
from unittest.mock import patch, MagicMock
import pytest
from app import app, normalize_audio_to_16k
from transcription.timestamping.aligner import AlignmentResult, VerseInterval, WordInterval

def make_dummy_wav(duration_ms=1000, sample_rate=44100):
    buf = io.BytesIO()
    num_frames = int(sample_rate * (duration_ms / 1000.0))
    with wave.open(buf, 'wb') as wf:
        wf.setnchannels(2)  # Stereo to test resampling/mono downmixing
        wf.setsampwidth(2)
        wf.setframerate(sample_rate)
        wf.writeframes(b'\x00\x00\x00\x00' * num_frames)
    buf.seek(0)
    return buf

@pytest.fixture
def client():
    app.config["TESTING"] = True
    with app.test_client() as client:
        yield client

def test_health_endpoint(client):
    res = client.get("/health")
    assert res.status_code == 200
    assert res.json == {"status": "ok"}

def test_normalize_audio_to_16k():
    wav_buf = make_dummy_wav(duration_ms=1000, sample_rate=44100)
    audio_seg = normalize_audio_to_16k(wav_buf.read())
    assert audio_seg.frame_rate == 16000
    assert audio_seg.channels == 1
    assert len(audio_seg) == 1000

@patch("app.get_best_model")
@patch("app.align_audio_segment")
def test_align_segment_success(mock_align, mock_get_model, client):
    mock_get_model.return_value = (MagicMock(), MagicMock(), "cpu")
    mock_alignment = AlignmentResult(
        audio_source="test.wav",
        verses=[
            VerseInterval(
                line_id="seg_0",
                cherokee_syllabary="ᎣᏏᏲ ᏏᏲ",
                raw_phonetic="osiyo siyo",
                english="",
                start_sec=0.1,
                end_sec=0.9,
                words=[
                    WordInterval(word="osiyo", start_sec=0.1, end_sec=0.5, confidence=0.96, cherokee_syllabary="ᎣᏏᏲ"),
                    WordInterval(word="siyo", start_sec=0.5, end_sec=0.9, confidence=0.94, cherokee_syllabary="ᏏᏲ"),
                ]
            )
        ]
    )
    mock_align.return_value = mock_alignment

    wav_buf = make_dummy_wav(duration_ms=1000, sample_rate=44100)
    data = {
        "audio": (wav_buf, "segment.wav"),
        "transcript": "ᎣᏏᏲ ᏏᏲ",
        "script_type": "syllabary"
    }
    res = client.post("/v1/align/segment", data=data, content_type="multipart/form-data")
    assert res.status_code == 200
    payload = res.json
    assert payload["status"] == "success"
    assert payload["script_type"] == "syllabary"
    words = payload["words"]
    assert len(words) == 2
    assert words[0]["text"] == "ᎣᏏᏲ"
    assert words[0]["start_ms"] == 100
    assert words[0]["end_ms"] == 500
    assert words[0]["confidence"] == 0.96
    assert words[1]["text"] == "ᏏᏲ"
    assert words[1]["start_ms"] == 500
    assert words[1]["end_ms"] == 900
    assert words[1]["confidence"] == 0.94

@patch("app.get_best_model")
@patch("app.align_audio_segment")
def test_align_segment_fallback_uniform_slicing(mock_align, mock_get_model, client):
    mock_get_model.return_value = (MagicMock(), MagicMock(), "cpu")
    # Empty words triggers uniform fallback
    mock_align.return_value = AlignmentResult(
        audio_source="test.wav",
        verses=[
            VerseInterval(
                line_id="seg_0",
                cherokee_syllabary="ᎣᏏᏲ ᏏᏲ",
                raw_phonetic="osiyo siyo",
                english="",
                start_sec=0.0,
                end_sec=0.0,
                words=[]
            )
        ]
    )
    wav_buf = make_dummy_wav(duration_ms=1000)
    data = {
        "audio": (wav_buf, "segment.wav"),
        "transcript": "ᎣᏏᏲ ᏏᏲ",
        "script_type": "syllabary"
    }
    res = client.post("/v1/align/segment", data=data, content_type="multipart/form-data")
    assert res.status_code == 200
    payload = res.json
    assert payload["status"] == "success"
    words = payload["words"]
    assert len(words) == 2
    assert words[0]["start_ms"] == 0
    assert words[0]["end_ms"] == 500
    assert words[1]["start_ms"] == 500
    assert words[1]["end_ms"] == 1000

def test_align_segment_missing_params(client):
    res = client.post("/v1/align/segment", data={}, content_type="multipart/form-data")
    assert res.status_code == 400

