package org.cherokee.elan.audio;

import javax.sound.sampled.*;
import java.io.*;

public class AudioSlicer {

    /**
     * Extracts a segment of audio from sourceMedia between startMs and endMs,
     * normalized to 16kHz, 16-bit, Mono PCM WAV bytes.
     */
    public static byte[] extractSegmentToWav(File sourceMedia, long startMs, long endMs) throws Exception {
        if (!sourceMedia.exists()) {
            throw new FileNotFoundException("Media file not found: " + sourceMedia.getAbsolutePath());
        }
        if (endMs < startMs) {
            throw new IllegalArgumentException("endMs cannot be smaller than startMs");
        }

        try (AudioInputStream sourceStream = AudioSystem.getAudioInputStream(sourceMedia)) {
            AudioFormat baseFormat = sourceStream.getFormat();
            
            // Normalize to 16kHz, 16-bit, Mono PCM for alignment pipeline
            AudioFormat targetFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                16000.0f, 16, 1, 2, 16000.0f, false
            );

            AudioInputStream convertedStream;
            if (baseFormat.matches(targetFormat)) {
                convertedStream = sourceStream;
            } else {
                convertedStream = AudioSystem.getAudioInputStream(targetFormat, sourceStream);
            }
            
            long bytesPerMillisecond = (long) (16000.0f * 2 / 1000.0);
            long bytesToSkip = startMs * bytesPerMillisecond;
            long bytesToRead = (endMs - startMs) * bytesPerMillisecond;

            long totalSkipped = 0;
            while (totalSkipped < bytesToSkip) {
                long skipped = convertedStream.skip(bytesToSkip - totalSkipped);
                if (skipped <= 0) {
                    // fallback to reading if skip returns 0
                    byte[] drop = new byte[(int) Math.min(4096, bytesToSkip - totalSkipped)];
                    int read = convertedStream.read(drop);
                    if (read == -1) break;
                    totalSkipped += read;
                } else {
                    totalSkipped += skipped;
                }
            }

            byte[] segmentData = convertedStream.readNBytes((int) bytesToRead);
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (AudioInputStream segmentStream = new AudioInputStream(
                    new ByteArrayInputStream(segmentData), targetFormat, segmentData.length / targetFormat.getFrameSize())) {
                AudioSystem.write(segmentStream, AudioFileFormat.Type.WAVE, out);
            }
            return out.toByteArray();
        }
    }
}
