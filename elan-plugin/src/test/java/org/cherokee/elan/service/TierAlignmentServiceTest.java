package org.cherokee.elan.service;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.LinguisticType;
import org.cherokee.elan.client.AlignmentClient;
import org.cherokee.elan.model.AlignmentResponse;
import org.cherokee.elan.model.WordAlignment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TierAlignmentServiceTest {

    private File createTestWav(Path tempDir, int durationSeconds) throws Exception {
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        byte[] pcmData = new byte[16000 * 2 * durationSeconds];
        File file = tempDir.resolve("test_service.wav").toFile();
        try (AudioInputStream ais = new AudioInputStream(
                new ByteArrayInputStream(pcmData), format, pcmData.length / format.getFrameSize())) {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
        }
        return file;
    }

    private LinguisticType createAlignableLinguisticType(TranscriptionImpl transcription) {
        LinguisticType type = new LinguisticType("default-lt");
        type.setTimeAlignable(true);
        transcription.addLinguisticType(type);
        return type;
    }

    @Test
    public void testAlignAndPopulateSuccessful(@TempDir Path tempDir) throws Exception {
        File wav = createTestWav(tempDir, 4);

        TranscriptionImpl transcription = new TranscriptionImpl();
        LinguisticType lt = createAlignableLinguisticType(transcription);

        TierImpl sourceTier = new TierImpl("SourceTier", "speaker1", transcription, lt);
        TierImpl targetTier = new TierImpl("TargetTier", "speaker1", transcription, lt);
        transcription.addTier(sourceTier);
        transcription.addTier(targetTier);

        Annotation srcAnn = sourceTier.createAnnotation(1000, 3000);
        srcAnn.setValue("ᎣᏏᏲ ᏏᏲ");

        AlignmentClient mockClient = new AlignmentClient() {
            @Override
            public AlignmentResponse alignSegment(byte[] wavBytes, String transcript, String scriptType) throws IOException {
                AlignmentResponse res = new AlignmentResponse();
                try {
                    var fieldStatus = AlignmentResponse.class.getDeclaredField("status");
                    fieldStatus.setAccessible(true);
                    fieldStatus.set(res, "success");

                    var fieldWords = AlignmentResponse.class.getDeclaredField("words");
                    fieldWords.setAccessible(true);
                    fieldWords.set(res, Arrays.asList(
                        new WordAlignment("ᎣᏏᏲ", 0, 900, 0.95),
                        new WordAlignment("ᏏᏲ", 1000, 1900, 0.92)
                    ));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return res;
            }
        };

        TierAlignmentService service = new TierAlignmentService(mockClient);
        List<Annotation> results = service.alignAndPopulate(wav, srcAnn, targetTier, "syllabary", false);

        assertEquals(2, results.size());
        assertEquals("ᎣᏏᏲ", results.get(0).getValue());
        assertEquals(1000, results.get(0).getBeginTimeBoundary()); // 1000 + 0
        assertEquals(1900, results.get(0).getEndTimeBoundary());   // 1000 + 900

        assertEquals("ᏏᏲ", results.get(1).getValue());
        assertEquals(2000, results.get(1).getBeginTimeBoundary()); // 1000 + 1000
        assertEquals(2900, results.get(1).getEndTimeBoundary());   // 1000 + 1900
    }

    @Test
    public void testCollisionDetectionAndOverwrite(@TempDir Path tempDir) throws Exception {
        File wav = createTestWav(tempDir, 4);

        TranscriptionImpl transcription = new TranscriptionImpl();
        LinguisticType lt = createAlignableLinguisticType(transcription);

        TierImpl sourceTier = new TierImpl("SourceTier", "speaker1", transcription, lt);
        TierImpl targetTier = new TierImpl("TargetTier", "speaker1", transcription, lt);
        transcription.addTier(sourceTier);
        transcription.addTier(targetTier);

        Annotation srcAnn = sourceTier.createAnnotation(1000, 3000);
        srcAnn.setValue("ᎣᏏᏲ");

        // Existing conflicting annotation on target tier
        targetTier.createAnnotation(1500, 2500);

        AlignmentClient mockClient = new AlignmentClient() {
            @Override
            public AlignmentResponse alignSegment(byte[] wavBytes, String transcript, String scriptType) {
                AlignmentResponse res = new AlignmentResponse();
                try {
                    var fieldStatus = AlignmentResponse.class.getDeclaredField("status");
                    fieldStatus.setAccessible(true);
                    fieldStatus.set(res, "success");
                    var fieldWords = AlignmentResponse.class.getDeclaredField("words");
                    fieldWords.setAccessible(true);
                    fieldWords.set(res, Arrays.asList(new WordAlignment("ᎣᏏᏲ", 0, 2000, 0.95)));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return res;
            }
        };

        TierAlignmentService service = new TierAlignmentService(mockClient);

        // Overwrite = false should throw
        assertThrows(IllegalStateException.class, () -> {
            service.alignAndPopulate(wav, srcAnn, targetTier, "syllabary", false);
        });

        // Overwrite = true should succeed and clear the conflicting annotation
        List<Annotation> results = service.alignAndPopulate(wav, srcAnn, targetTier, "syllabary", true);
        assertEquals(1, results.size());
        assertEquals(1, targetTier.getAnnotations().size());
    }
}
