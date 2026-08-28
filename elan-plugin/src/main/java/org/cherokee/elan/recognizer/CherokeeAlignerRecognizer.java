package org.cherokee.elan.recognizer;

import mpi.eudico.client.annotator.recognizer.api.AbstractSelectionPanel;
import mpi.eudico.client.annotator.recognizer.api.Recognizer;
import mpi.eudico.client.annotator.recognizer.api.RecognizerConfigurationException;
import mpi.eudico.client.annotator.recognizer.api.RecognizerHost;
import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segment;
import mpi.eudico.client.annotator.recognizer.data.Segmentation;
import org.cherokee.elan.audio.AudioSlicer;
import org.cherokee.elan.client.AlignmentClient;
import org.cherokee.elan.model.AlignmentResponse;
import org.cherokee.elan.model.WordAlignment;

import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.recognizer.io.CsvTierIO;
import mpi.eudico.client.annotator.recognizer.io.XmlTierIO;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.event.ACMEditEvent;

import javax.swing.JPanel;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Cherokee forced-alignment recognizer plugin for ELAN.
 */
public class CherokeeAlignerRecognizer implements Recognizer, Runnable {

    private String name = "Cherokee Forced Aligner";
    private RecognizerHost host;
    private List<String> mediaPaths = new ArrayList<>();
    private final Map<String, Object> parameters = new HashMap<>();
    private final AlignmentClient customAlignmentClient;
    private Thread workerThread;
    private volatile boolean isRunning = false;
    private CherokeeAlignerPanel controlPanel;

    public CherokeeAlignerRecognizer() {
        this(null);
    }

    public CherokeeAlignerRecognizer(AlignmentClient alignmentClient) {
        this.customAlignmentClient = alignmentClient;
        parameters.put("server_url", "http://localhost:5050");
        parameters.put("script_type", "syllabary");
    }

    public AlignmentClient getAlignmentClient(String serverUrl) {
        if (this.customAlignmentClient != null) {
            return this.customAlignmentClient;
        }
        return new AlignmentClient(serverUrl);
    }

    @Override
    public boolean setMedia(List<String> mediaFiles) {
        this.mediaPaths = (mediaFiles != null) ? new ArrayList<>(mediaFiles) : new ArrayList<>();
        return !this.mediaPaths.isEmpty();
    }

    @Override
    public boolean canHandleMedia(String mediaFile) {
        if (mediaFile == null) {
            return false;
        }
        String lower = mediaFile.toLowerCase();
        return lower.endsWith(".wav") || lower.endsWith(".mp3") || lower.endsWith(".mp4") || lower.endsWith(".mov");
    }

    @Override
    public boolean canCombineMultipleFiles() {
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getRecognizerType() {
        return Recognizer.AUDIO_TYPE;
    }

    @Override
    public void setRecognizerHost(RecognizerHost host) {
        this.host = host;
    }

    @Override
    public JPanel getControlPanel() {
        if (controlPanel == null) {
            AbstractSelectionPanel selPanel = (host != null) ? host.getSelectionPanel(null) : null;
            List<String> tierNames = getTranscriptionTierNames();
            controlPanel = new CherokeeAlignerPanel(selPanel, tierNames);
            if (parameters.containsKey("server_url")) {
                controlPanel.setServerUrl((String) parameters.get("server_url"));
            }
            if (parameters.containsKey("script_type")) {
                controlPanel.setScriptType((String) parameters.get("script_type"));
            }
            if (parameters.containsKey("target_tier")) {
                controlPanel.setTargetTierName((String) parameters.get("target_tier"));
            }
        }
        return controlPanel;
    }

    @Override
    public void setParameterValue(String paramName, String value) {
        parameters.put(paramName, value);
        if (controlPanel != null) {
            if ("server_url".equals(paramName)) {
                controlPanel.setServerUrl(value);
            } else if ("script_type".equals(paramName)) {
                controlPanel.setScriptType(value);
            } else if ("target_tier".equals(paramName)) {
                controlPanel.setTargetTierName(value);
            }
        }
    }

    @Override
    public void setParameterValue(String paramName, float value) {
        parameters.put(paramName, value);
    }

    @Override
    public Object getParameterValue(String paramName) {
        if (controlPanel != null) {
            if ("server_url".equals(paramName)) {
                return controlPanel.getServerUrl();
            } else if ("script_type".equals(paramName)) {
                return controlPanel.getScriptType();
            } else if ("target_tier".equals(paramName)) {
                return controlPanel.getTargetTierName();
            }
        }
        return parameters.get(paramName);
    }

    @Override
    public void updateLocale(Locale locale) {
        if (controlPanel != null) {
            controlPanel.updateLocale(locale);
        }
    }

    @Override
    public void updateLocaleBundle(ResourceBundle bundle) {
        if (controlPanel != null) {
            controlPanel.updateLocaleBundle(bundle);
        }
    }

    @Override
    public void validateParameters() throws RecognizerConfigurationException {
        if (mediaPaths.isEmpty()) {
            throw new RecognizerConfigurationException("No media file specified for alignment.");
        }
        File mediaFile = new File(mediaPaths.get(0));
        if (!mediaFile.exists()) {
            throw new RecognizerConfigurationException("Media file not found: " + mediaFile.getAbsolutePath());
        }
        if (controlPanel != null) {
            controlPanel.validateParameters();
        }
    }

    @Override
    public void start() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        workerThread = new Thread(this, "CherokeeAlignerRecognizerThread");
        workerThread.start();
    }

    @Override
    public void stop() {
        isRunning = false;
        if (workerThread != null && workerThread.isAlive()) {
            workerThread.interrupt();
        }
    }

    @Override
    public void dispose() {
        stop();
        mediaPaths.clear();
        parameters.clear();
        controlPanel = null;
    }

    @Override
    public void run() {
        if (host == null) {
            return;
        }
        isRunning = true;

        try {
            host.appendToReport("Starting Cherokee forced-alignment recognizer...\n");
            host.setProgress(0.0f, "Initializing alignment...");

            if (mediaPaths.isEmpty()) {
                host.errorOccurred("No media file provided.");
                return;
            }

            File audioFile = new File(mediaPaths.get(0));
            String serverUrl = (controlPanel != null)
                    ? controlPanel.getServerUrl()
                    : (String) parameters.getOrDefault("server_url", "http://localhost:5050");
            String scriptType = (controlPanel != null)
                    ? controlPanel.getScriptType()
                    : (String) parameters.getOrDefault("script_type", "syllabary");

            AlignmentClient client = getAlignmentClient(serverUrl);

            String sourceTierName = "sentences";
            List<Segmentation> segmentations = null;
            if (parameters.containsKey("tier")) {
                segmentations = loadSegmentationsFromObject(parameters.get("tier"));
            }
            if (segmentations == null || segmentations.isEmpty()) {
                segmentations = loadInputSegmentations();
            }
            if (segmentations == null || segmentations.isEmpty()) {
                host.errorOccurred("No input tier or segmentation selected.");
                return;
            }

            if (!segmentations.isEmpty() && segmentations.get(0).getName() != null && !segmentations.get(0).getName().trim().isEmpty()) {
                sourceTierName = segmentations.get(0).getName().trim();
            }

            // Determine target tier name
            String targetTierName = (controlPanel != null)
                    ? controlPanel.getTargetTierName()
                    : null;
            if (parameters.containsKey("target_tier")) {
                List<Segmentation> targetSegs = loadSegmentationsFromObject(parameters.get("target_tier"));
                if (targetSegs != null && !targetSegs.isEmpty() && targetSegs.get(0).getName() != null && !targetSegs.get(0).getName().trim().isEmpty()) {
                    targetTierName = targetSegs.get(0).getName().trim();
                } else if (parameters.get("target_tier") instanceof String) {
                    String str = (String) parameters.get("target_tier");
                    if (!str.trim().isEmpty()) {
                        targetTierName = str.trim();
                    }
                }
            }
            if (targetTierName == null || targetTierName.trim().isEmpty()) {
                targetTierName = "words";
            }

            ViewerManager2 vm = extractViewerManager();
            Transcription transcription = (vm != null) ? vm.getTranscription() : null;
            Tier targetTier = null;
            if (transcription != null) {
                targetTier = transcription.getTierWithId(targetTierName);
                if (targetTier == null) {
                    host.errorOccurred("Target tier '" + targetTierName + "' does not exist in the transcription.");
                    return;
                }
            }

            ArrayList<RSelection> outputWords = new ArrayList<>();
            int totalSegs = 0;
            for (Segmentation seg : segmentations) {
                if (seg.getSegments() != null) {
                    totalSegs += seg.getSegments().size();
                }
            }

            int processed = 0;
            int directlyPopulatedCount = 0;
            for (Segmentation inputSegmentation : segmentations) {
                if (!isRunning) {
                    break;
                }
                List<RSelection> segments = inputSegmentation.getSegments();
                if (segments == null) {
                    continue;
                }

                for (RSelection item : segments) {
                    if (!isRunning) {
                        break;
                    }

                    long tStart = item.beginTime;
                    long tEnd = item.endTime;
                    String transcript = "";
                    if (item instanceof Segment) {
                        transcript = ((Segment) item).label;
                    }

                    if (transcript != null && !transcript.trim().isEmpty() && (tEnd > tStart)) {
                        try {
                            byte[] audioBytes = AudioSlicer.extractSegmentToWav(audioFile, tStart, tEnd);
                            AlignmentResponse response = client.alignSegment(audioBytes, transcript, scriptType);

                            if (response != null && "success".equalsIgnoreCase(response.getStatus()) && response.getWords() != null) {
                                // If target tier exists in transcription, clear overlapping annotations in this interval
                                if (targetTier instanceof TierImpl) {
                                    TierImpl tierImpl = (TierImpl) targetTier;
                                    List<Annotation> toRemove = new ArrayList<>();
                                    for (Object annObj : tierImpl.getAnnotations()) {
                                        if (annObj instanceof Annotation) {
                                            Annotation ann = (Annotation) annObj;
                                            if (ann.getBeginTimeBoundary() < tEnd && ann.getEndTimeBoundary() > tStart) {
                                                toRemove.add(ann);
                                            }
                                        }
                                    }
                                    for (Annotation ann : toRemove) {
                                        tierImpl.removeAnnotation(ann);
                                    }
                                }

                                for (WordAlignment word : response.getWords()) {
                                    long wordAbsBegin = tStart + word.getStartMs();
                                    long wordAbsEnd = Math.min(tStart + word.getEndMs(), tEnd);
                                    if (wordAbsBegin < wordAbsEnd) {
                                        outputWords.add(new Segment(wordAbsBegin, wordAbsEnd, word.getText()));

                                        if (targetTier instanceof TierImpl) {
                                            Annotation newAnn = ((TierImpl) targetTier).createAnnotation(wordAbsBegin, wordAbsEnd);
                                            if (newAnn != null) {
                                                newAnn.setValue(word.getText());
                                                directlyPopulatedCount++;
                                            }
                                        }
                                    }
                                }
                            } else {
                                String err = (response != null && response.getError() != null) ? response.getError() : "Unknown response";
                                host.appendToReport("Alignment warning for segment [" + tStart + "-" + tEnd + "]: " + err + "\n");
                            }
                        } catch (Exception ex) {
                            host.appendToReport("Error processing segment [" + tStart + "-" + tEnd + "]: " + ex.getMessage() + "\n");
                        }
                    }

                    processed++;
                    float progress = (totalSegs > 0) ? ((float) processed / totalSegs) : 1.0f;
                    host.setProgress(progress, "Aligned " + processed + "/" + totalSegs + " segments");
                }
            }

            if (directlyPopulatedCount > 0 && transcription instanceof TranscriptionImpl && targetTier != null) {
                ((TranscriptionImpl) transcription).notifyListeners(targetTier, ACMEditEvent.CHANGE_ANNOTATIONS, null);
                transcription.setChanged();
                host.appendToReport("Directly populated " + directlyPopulatedCount + " annotations on existing tier '" + targetTier.getName() + "'.\n");
            }

            if (!outputWords.isEmpty()) {
                if (directlyPopulatedCount == 0) {
                    Segmentation resultSegmentation = new Segmentation(targetTierName, outputWords, "words");
                    host.addSegmentation(resultSegmentation);
                }
                host.appendToReport("Successfully aligned " + outputWords.size() + " words.\n");
            } else {
                host.appendToReport("No word alignments were generated.\n");
            }

            host.setProgress(1.0f, "Completed alignment");
        } catch (Exception e) {
            host.errorOccurred("Alignment recognizer error: " + e.getMessage());
            host.appendToReport("Fatal error: " + e.getMessage() + "\n");
        } finally {
            isRunning = false;
        }
    }

    private ViewerManager2 extractViewerManager() {
        if (host == null) {
            return null;
        }
        try {
            Class<?> clazz = host.getClass();
            while (clazz != null) {
                try {
                    Field f = clazz.getDeclaredField("viewerManager");
                    f.setAccessible(true);
                    Object vm = f.get(host);
                    if (vm instanceof ViewerManager2) {
                        return (ViewerManager2) vm;
                    }
                } catch (NoSuchFieldException ignored) {
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private List<Segmentation> loadSegmentationsFromObject(Object val) {
        if (val instanceof String) {
            File f = new File((String) val);
            if (f.exists() && f.isFile()) {
                try {
                    XmlTierIO xmlReader = new XmlTierIO(f);
                    List<Segmentation> segs = xmlReader.parse();
                    if (segs != null && !segs.isEmpty()) {
                        return segs;
                    }
                } catch (Exception ex) {
                    try {
                        CsvTierIO csvReader = new CsvTierIO();
                        List<Segmentation> segs = csvReader.read(f);
                        if (segs != null && !segs.isEmpty()) {
                            return segs;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    private List<Segmentation> loadInputSegmentations() {
        // 1. Try checking parameters for tier file path (e.g. key "tier" or any param with a valid file)
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if ("target_tier".equals(entry.getKey())) {
                continue;
            }
            List<Segmentation> segs = loadSegmentationsFromObject(entry.getValue());
            if (!segs.isEmpty()) {
                if (host != null) {
                    host.appendToReport("Loaded " + segs.size() + " segmentation(s) from parameter '" + entry.getKey() + "'.\n");
                }
                return segs;
            }
        }

        // 2. Try checking control panel selections if available
        if (controlPanel != null) {
            List<RSelection> selections = controlPanel.getSelections();
            if (selections != null && !selections.isEmpty()) {
                Segmentation seg = new Segmentation("Selection", new ArrayList<>(selections), "Selection");
                return Collections.singletonList(seg);
            }
        }

        // 3. Fallback to host.getSegmentations() if available
        if (host != null) {
            List<Segmentation> hostSegs = host.getSegmentations();
            if (hostSegs != null && !hostSegs.isEmpty()) {
                return hostSegs;
            }
        }

        return Collections.emptyList();
    }

    private List<String> getTranscriptionTierNames() {
        List<String> tierNames = new ArrayList<>();
        ViewerManager2 vm = extractViewerManager();
        if (vm != null && vm.getTranscription() != null) {
            Transcription transcription = vm.getTranscription();
            List<? extends Tier> tiers = transcription.getTiers();
            if (tiers != null) {
                for (Tier tier : tiers) {
                    if (tier != null && tier.getName() != null) {
                        tierNames.add(tier.getName());
                    }
                }
            }
        }
        return tierNames;
    }
}
