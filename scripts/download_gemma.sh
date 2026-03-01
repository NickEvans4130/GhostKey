#!/usr/bin/env bash
# Download Gemma 2B INT4 quantized model for GhostKey Tier 2 inference.
#
# The model is downloaded from Hugging Face and placed in the app's internal
# files directory. The app's ModelManager detects it at runtime via:
#   context.filesDir/models/gemma-2b-it-gpu-int4.bin
#
# For development: push the model to a connected device/emulator:
#   adb push gemma-2b-it-gpu-int4.bin /data/data/com.ghostkey/files/models/
#
# Hugging Face model page:
#   https://huggingface.co/google/gemma-2b-it-gpu-int4
#
# You must accept the Gemma license at the above URL before downloading.

set -euo pipefail

MODEL_FILENAME="gemma-2b-it-gpu-int4.bin"
OUTPUT_DIR="${1:-./model_cache}"

mkdir -p "$OUTPUT_DIR"

echo "Downloading $MODEL_FILENAME to $OUTPUT_DIR ..."
echo "Note: ~1.3 GB — ensure sufficient disk space."
echo ""
echo "If you have a Hugging Face token, set HF_TOKEN=<your-token> in your environment."

HF_URL="https://huggingface.co/google/gemma-2b-it-gpu-int4/resolve/main/${MODEL_FILENAME}"

if [ -n "${HF_TOKEN:-}" ]; then
    curl -L -H "Authorization: Bearer $HF_TOKEN" "$HF_URL" -o "$OUTPUT_DIR/$MODEL_FILENAME"
else
    curl -L "$HF_URL" -o "$OUTPUT_DIR/$MODEL_FILENAME"
fi

echo ""
echo "Downloaded to $OUTPUT_DIR/$MODEL_FILENAME"
echo ""
echo "To push to a connected Android device:"
echo "  adb shell mkdir -p /data/data/com.ghostkey/files/models"
echo "  adb push $OUTPUT_DIR/$MODEL_FILENAME /data/data/com.ghostkey/files/models/"
