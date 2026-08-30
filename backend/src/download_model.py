"""
Preloads and caches the Cherokee ASR model weights and processor from Hugging Face during container build.
"""
import logging
from transcription.models.asr_model import CherokeeASRModel

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger(__name__)

def main():
    logger.info("Pre-downloading Cherokee ASR model weights and processor...")
    asr_model = CherokeeASRModel.get_best_model()
    logger.info("Successfully cached model on device: %s", asr_model.device)

if __name__ == "__main__":
    main()
