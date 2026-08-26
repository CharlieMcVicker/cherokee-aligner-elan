package org.cherokee.elan.client;

import org.cherokee.elan.model.AlignmentResponse;
import org.cherokee.elan.model.WordAlignment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AlignmentClientIntegrationTest {

    private byte[] makeWavBytes(int sampleRate, int durationSeconds) throws Exception {
        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
        byte[] pcmData = new byte[sampleRate * 2 * durationSeconds];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (AudioInputStream ais = new AudioInputStream(
                new ByteArrayInputStream(pcmData), format, pcmData.length / format.getFrameSize())) {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, out);
        }
        return out.toByteArray();
    }

    @Test
    public void testLiveBackendCall() throws Exception {
        String backendUrl = System.getProperty("aligner.backend.url");
        if (backendUrl == null || backendUrl.trim().isEmpty()) {
            // Integration test skipped if live server not specified
            return;
        }

        AlignmentClient client = new AlignmentClient(backendUrl);
        byte[] wavBytes = makeWavBytes(16000, 2);
        AlignmentResponse res = client.alignSegment(wavBytes, "ᎣᏏᏲ ᏂᎯ", "syllabary");

        assertNotNull(res);
        assertEquals("success", res.getStatus());
        assertEquals("syllabary", res.getScriptType());
        List<WordAlignment> words = res.getWords();
        assertNotNull(words);
        assertEquals(2, words.size());
        assertEquals("ᎣᏏᏲ", words.get(0).getText());
        assertEquals("ᏂᎯ", words.get(1).getText());
    }
}
