FROM python:3.12-slim

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    ffmpeg \
    build-essential \
    git \
    && rm -rf /var/lib/apt/lists/*

COPY pyproject.toml .
RUN mkdir -p src && pip install --no-cache-dir .

# Revision hash for workshop-transcription (update this variable to bust cache & rebuild dep)
ENV TRANSCRIPTION_REVISION="4132df6e2be3fb7c2fecf53c69c7cfff273e115a"

# Reinstall git dependency at the specified revision
RUN pip install --no-cache-dir --force-reinstall --no-deps "workshop-transcription @ git+https://github.com/charliemcvicker/cherokee-ASR.git@${TRANSCRIPTION_REVISION}"

COPY . .

# Pre-download and cache model weights so image is self-contained and offline-ready
ENV HF_HOME="/root/.cache/huggingface"
ENV PYTHONPATH="/app/src"
RUN python src/download_model.py

EXPOSE 5050
CMD ["python", "src/app.py"]

