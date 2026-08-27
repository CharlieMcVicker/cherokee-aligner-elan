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

# TODO can we skip venv somehow? we are copying that dir even though it is not needed. can we copy . but not some files?
COPY . .

EXPOSE 5050
CMD ["python", "app.py"]
