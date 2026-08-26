package org.cherokee.elan.audio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class AudioSlicerTest {

    private File createTestWav(Path tempDir, int sampleRate, int durationSeconds) throws Exception {
        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
        byte[] pcmData = new byte[sampleRate * 2 * durationSeconds];
        
        File file = tempDir.resolve("test_input.wav").toFile();
        try (AudioInputStream ais = new AudioInputStream(
                new ByteArrayInputStream(pcmData), format, pcmData.length / format.getFrameSize())) {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
        }
        return file;
    }

    @Test
    public void testExtractSegmentToWav(@TempDir Path tempDir) throws Exception {
        File wav = createTestWav(tempDir, 16000, 5);
        
        // Extract 1000ms to 3000ms (2000ms = 2s)
        byte[] slicedWav = AudioSlicer.extractSegmentToWav(wav, 1000, 3000);
        assertNotNull(slicedWav);
        assertTrue(slicedWav.length > 0);

        try (AudioInputStream slicedAis = AudioSystem.getAudioInputStream(new ByteArrayInputStream(slicedWav))) {
            AudioFormat format = slicedAis.getFormat();
            assertEquals(16000.0f, format.getSampleRate());
            assertEquals(16, format.getSampleSizeInBits());
            assertEquals(1, format.getChannels());
            
            long frameLength = slicedAis.getFrameLength();
            assertEquals(32000, frameLength); // 2 seconds * 16000 frames/sec
        }
    }
}
