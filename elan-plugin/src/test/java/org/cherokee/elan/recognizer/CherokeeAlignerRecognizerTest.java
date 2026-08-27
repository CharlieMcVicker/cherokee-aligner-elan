package org.cherokee.elan.recognizer;

import mpi.eudico.client.annotator.recognizer.api.AbstractSelectionPanel;
import mpi.eudico.client.annotator.recognizer.api.Recognizer;
import mpi.eudico.client.annotator.recognizer.api.RecognizerConfigurationException;
import mpi.eudico.client.annotator.recognizer.api.RecognizerHost;
import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segment;
import mpi.eudico.client.annotator.recognizer.data.Segmentation;
import org.cherokee.elan.client.AlignmentClient;
import org.cherokee.elan.model.AlignmentResponse;
import org.cherokee.elan.model.WordAlignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.JPanel;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CherokeeAlignerRecognizerTest {

    @TempDir
    Path tempDir;

    private File dummyWav;

    @BeforeEach
    void setUp() throws IOException {
        dummyWav = tempDir.resolve("test.wav").toFile();
        AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, false);
        byte[] audioBytes = new byte[16000 * 2]; // 1 second of silence
        try (AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(audioBytes), format, 16000)) {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, dummyWav);
        }
    }

    @Test
    void testBasicPropertiesAndMediaValidation() throws Exception {
        CherokeeAlignerRecognizer recognizer = new CherokeeAlignerRecognizer();
        assertEquals("Cherokee Forced Aligner", recognizer.getName());
        assertEquals(Recognizer.AUDIO_TYPE, recognizer.getRecognizerType());
        assertFalse(recognizer.canCombineMultipleFiles());

        assertTrue(recognizer.canHandleMedia("sample.wav"));
        assertTrue(recognizer.canHandleMedia("sample.mp4"));
        assertFalse(recognizer.canHandleMedia("sample.txt"));
        assertFalse(recognizer.canHandleMedia(null));

        assertThrows(RecognizerConfigurationException.class, recognizer::validateParameters);

        recognizer.setMedia(Collections.singletonList(dummyWav.getAbsolutePath()));
        assertDoesNotThrow(recognizer::validateParameters);

        recognizer.setParameterValue("script_type", "latin");
        assertEquals("latin", recognizer.getParameterValue("script_type"));
    }

    @Test
    void testControlPanelCreationAndPreferences() {
        CherokeeAlignerRecognizer recognizer = new CherokeeAlignerRecognizer();
        TestRecognizerHost host = new TestRecognizerHost();
        recognizer.setRecognizerHost(host);

        JPanel panel = recognizer.getControlPanel();
        assertNotNull(panel);
        assertTrue(panel instanceof CherokeeAlignerPanel);

        CherokeeAlignerPanel alignerPanel = (CherokeeAlignerPanel) panel;
        assertEquals("http://localhost:5050", alignerPanel.getServerUrl());
        assertEquals("syllabary", alignerPanel.getScriptType());
        assertEquals("words", alignerPanel.getTargetTierName());
        assertTrue(alignerPanel.isAutoCreateTargetTier());

        recognizer.setParameterValue("server_url", "https://align.example.com:8080");
        assertEquals("https://align.example.com:8080", alignerPanel.getServerUrl());
        assertEquals("https://align.example.com:8080", recognizer.getParameterValue("server_url"));

        recognizer.setParameterValue("script_type", "latin");
        assertEquals("latin", alignerPanel.getScriptType());
        assertEquals("latin", recognizer.getParameterValue("script_type"));

        recognizer.setParameterValue("target_tier", "my_words");
        assertEquals("my_words", alignerPanel.getTargetTierName());
        assertEquals("my_words", recognizer.getParameterValue("target_tier"));
        assertFalse(alignerPanel.isAutoCreateTargetTier());

        Map<String, Object> prefs = alignerPanel.getParamPreferences();
        assertEquals("https://align.example.com:8080", prefs.get("server_url"));
        assertEquals("latin", prefs.get("script_type"));
        assertEquals("my_words", prefs.get("target_tier"));

        Map<String, Object> newPrefs = new HashMap<>();
        newPrefs.put("server_url", "http://127.0.0.1:5050");
        newPrefs.put("script_type", "syllabary");
        newPrefs.put("target_tier", "custom_words");
        alignerPanel.setParamPreferences(newPrefs);
        assertEquals("http://127.0.0.1:5050", alignerPanel.getServerUrl());
        assertEquals("syllabary", alignerPanel.getScriptType());
        assertEquals("custom_words", alignerPanel.getTargetTierName());
    }

    @Test
    void testServerUrlValidation() {
        CherokeeAlignerPanel panel = new CherokeeAlignerPanel(null);
        panel.setServerUrl("http://localhost:5050");
        assertDoesNotThrow(panel::validateServerUrl);

        panel.setServerUrl("https://server.domain.org:8443");
        assertDoesNotThrow(panel::validateServerUrl);

        // Invalid: empty
        panel.setServerUrl("");
        assertThrows(RecognizerConfigurationException.class, panel::validateServerUrl);

        // Invalid scheme
        panel.setServerUrl("ftp://localhost:5050");
        assertThrows(RecognizerConfigurationException.class, panel::validateServerUrl);

        // Invalid scheme missing http/https
        panel.setServerUrl("localhost:5050");
        assertThrows(RecognizerConfigurationException.class, panel::validateServerUrl);

        // Invalid port
        panel.setServerUrl("http://localhost:99999");
        assertThrows(RecognizerConfigurationException.class, panel::validateServerUrl);
    }

    @Test
    void testTargetTierDropdown() {
        List<String> availableTiers = List.of("sentences", "notes", "words");
        CherokeeAlignerPanel panel = new CherokeeAlignerPanel(null, availableTiers);

        // Default auto-create option
        assertEquals("words", panel.getTargetTierName());
        assertTrue(panel.isAutoCreateTargetTier());

        // Select existing tier
        panel.setTargetTierName("sentences");
        assertEquals("sentences", panel.getTargetTierName());
        assertFalse(panel.isAutoCreateTargetTier());

        // Select non-existing tier dynamically
        panel.setTargetTierName("new_tier");
        assertEquals("new_tier", panel.getTargetTierName());

        // Update available tiers
        panel.updateAvailableTiers(List.of("speaker1", "speaker2"));
        panel.setTargetTierName("[Auto-create: words]");
        assertEquals("words", panel.getTargetTierName());
        assertTrue(panel.isAutoCreateTargetTier());
    }

    @Test
    void testExecutionWithMockHost() throws Exception {
        AlignmentClient mockClient = new AlignmentClient() {
            @Override
            public AlignmentResponse alignSegment(byte[] wavBytes, String transcript, String scriptType) {
                AlignmentResponse response = new AlignmentResponse();
                response.setStatus("success");
                response.setScriptType(scriptType);
                List<WordAlignment> words = new ArrayList<>();
                words.add(new WordAlignment("ᎣᏏᏲ", 0, 400, 0.95));
                words.add(new WordAlignment("ᏏᏲ", 450, 800, 0.92));
                response.setWords(words);
                return response;
            }
        };

        CherokeeAlignerRecognizer recognizer = new CherokeeAlignerRecognizer(mockClient);
        recognizer.setMedia(Collections.singletonList(dummyWav.getAbsolutePath()));

        TestRecognizerHost host = new TestRecognizerHost();
        ArrayList<RSelection> segments = new ArrayList<>();
        segments.add(new Segment(0, 1000, "ᎣᏏᏲ ᏏᏲ"));
        Segmentation inputSeg = new Segmentation("Cherokee Source", segments, "Cherokee");
        host.segmentations.add(inputSeg);

        recognizer.setRecognizerHost(host);
        recognizer.run();

        assertEquals(1, host.addedSegmentations.size());
        Segmentation result = host.addedSegmentations.get(0);
        assertEquals(2, result.getSegments().size());
        assertEquals("ᎣᏏᏲ", ((Segment) result.getSegments().get(0)).label);
        assertEquals(0, result.getSegments().get(0).beginTime);
        assertEquals(400, result.getSegments().get(0).endTime);
        assertEquals("ᏏᏲ", ((Segment) result.getSegments().get(1)).label);
        assertEquals(450, result.getSegments().get(1).beginTime);
        assertEquals(800, result.getSegments().get(1).endTime);
    }

    @Test
    void testExecutionWithTierParamXmlFile() throws Exception {
        AlignmentClient mockClient = new AlignmentClient() {
            @Override
            public AlignmentResponse alignSegment(byte[] wavBytes, String transcript, String scriptType) {
                AlignmentResponse response = new AlignmentResponse();
                response.setStatus("success");
                response.setScriptType(scriptType);
                List<WordAlignment> words = new ArrayList<>();
                words.add(new WordAlignment("ᎣᏏᏲ", 0, 400, 0.95));
                words.add(new WordAlignment("ᏏᏲ", 450, 800, 0.92));
                response.setWords(words);
                return response;
            }
        };

        File xmlTierFile = tempDir.resolve("input_tier.xml").toFile();
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<TIERS xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "    <TIER columns=\"sentences\">\n" +
                "        <span start=\"0\" end=\"1000\">\n" +
                "            <v>ᎣᏏᏲ ᏏᏲ</v>\n" +
                "        </span>\n" +
                "    </TIER>\n" +
                "</TIERS>\n";
        java.nio.file.Files.write(xmlTierFile.toPath(), xmlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        CherokeeAlignerRecognizer recognizer = new CherokeeAlignerRecognizer(mockClient);
        recognizer.setMedia(Collections.singletonList(dummyWav.getAbsolutePath()));
        recognizer.setParameterValue("tier", xmlTierFile.getAbsolutePath());

        TestRecognizerHost host = new TestRecognizerHost();
        recognizer.setRecognizerHost(host);
        recognizer.run();

        assertEquals(1, host.addedSegmentations.size());
        Segmentation result = host.addedSegmentations.get(0);
        assertEquals(2, result.getSegments().size());
        assertEquals("ᎣᏏᏲ", ((Segment) result.getSegments().get(0)).label);
        assertEquals("ᏏᏲ", ((Segment) result.getSegments().get(1)).label);
    }

    static class TestRecognizerHost implements RecognizerHost {
        List<Segmentation> segmentations = new ArrayList<>();
        List<Segmentation> addedSegmentations = new ArrayList<>();
        List<String> reports = new ArrayList<>();
        String error = null;
        float progress = 0.0f;

        @Override
        public void addSegmentation(Segmentation s) {
            addedSegmentations.add(s);
        }

        @Override
        public void setProgress(float v) {
            this.progress = v;
        }

        @Override
        public void setProgress(float v, String s) {
            this.progress = v;
        }

        @Override
        public void errorOccurred(String s) {
            this.error = s;
        }

        @Override
        public void appendToReport(String s) {
            reports.add(s);
        }

        @Override
        public AbstractSelectionPanel getSelectionPanel(String s) {
            return null;
        }

        @Override
        public List<String> getMediaFiles(int i) {
            return Collections.emptyList();
        }

        @Override
        public List<Segmentation> getSegmentations() {
            return segmentations;
        }

        @Override
        public boolean isBusy() {
            return false;
        }
    }
}
