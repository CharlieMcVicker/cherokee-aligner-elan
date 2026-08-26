import io
import wave
import pytest
from app import app

def make_dummy_wav(duration_ms=1000, sample_rate=16000):
    buf = io.BytesIO()
    num_frames = int(sample_rate * (duration_ms / 1000.0))
    with wave.open(buf, 'wb') as wf:
        wf.setnchannels(1)
        wf.setsampwidth(2)
        wf.setframerate(sample_rate)
        wf.writeframes(b'\x00\x00' * num_frames)
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

def test_align_segment_success(client):
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
    assert payload["script_type"] == "syllabary"
    words = payload["words"]
    assert len(words) == 2
    assert words[0]["text"] == "ᎣᏏᏲ"
    assert words[0]["start_ms"] == 0
    assert words[0]["end_ms"] == 500
    assert words[1]["text"] == "ᏏᏲ"
    assert words[1]["start_ms"] == 500
    assert words[1]["end_ms"] == 1000

def test_align_segment_missing_params(client):
    res = client.post("/v1/align/segment", data={}, content_type="multipart/form-data")
    assert res.status_code == 400
