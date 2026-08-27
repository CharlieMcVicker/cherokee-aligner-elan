"""
Preloads and caches the Cherokee ASR model weights and processor from Hugging Face during container build.
"""
import logging
from transcription.utils.model_utils import get_best_model

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger(__name__)

def main():
    logger.info("Pre-downloading Cherokee ASR model weights and processor...")
    model, processor, model_name = get_best_model()
    logger.info("Successfully cached model: %s", model_name)

if __name__ == "__main__":
    main()
