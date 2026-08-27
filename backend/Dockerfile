FROM python:3.12-slim

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    ffmpeg \
    build-essential \
    git \
    && rm -rf /var/lib/apt/lists/*

COPY pyproject.toml .
RUN pip install --no-cache-dir .

# Revision hash for workshop-transcription (update this variable to bust cache & rebuild dep)
ENV TRANSCRIPTION_REVISION="44a503a544ef7338981ab0d75235005d9d1db943"

# Reinstall git dependency at the specified revision
RUN pip install --no-cache-dir --force-reinstall --no-deps "workshop-transcription @ git+https://github.com/charliemcvicker/cherokee-ASR.git@${TRANSCRIPTION_REVISION}"

COPY . .

# Pre-download and cache model weights so image is self-contained and offline-ready
ENV HF_HOME="/root/.cache/huggingface"
ENV PYTHONPATH="/app/src"
RUN python src/download_model.py

EXPOSE 5050
CMD ["python", "src/app.py"]

