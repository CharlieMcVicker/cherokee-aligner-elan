package org.cherokee.elan.service;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import org.cherokee.elan.audio.AudioSlicer;
import org.cherokee.elan.client.AlignmentClient;
import org.cherokee.elan.model.AlignmentResponse;
import org.cherokee.elan.model.WordAlignment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TierAlignmentService {

    private final AlignmentClient client;

    public TierAlignmentService() {
        this(new AlignmentClient());
    }

    public TierAlignmentService(AlignmentClient client) {
        this.client = client;
    }

    /**
     * Checks if there are any existing annotations on targetTier overlapping [startMs, endMs].
     */
    public List<Annotation> findOverlappingAnnotations(Tier targetTier, long startMs, long endMs) {
        List<Annotation> overlapping = new ArrayList<>();
        if (targetTier == null || targetTier.getAnnotations() == null) {
            return overlapping;
        }

        for (Object obj : targetTier.getAnnotations()) {
            if (obj instanceof Annotation) {
                Annotation ann = (Annotation) obj;
                long aStart = ann.getBeginTimeBoundary();
                long aEnd = ann.getEndTimeBoundary();
                // Overlap condition: aStart < endMs && aEnd > startMs
                if (aStart < endMs && aEnd > startMs) {
                    overlapping.add(ann);
                }
            }
        }
        return overlapping;
    }

    /**
     * Removes annotations from target tier.
     */
    public void removeAnnotations(Tier targetTier, List<Annotation> annotationsToRemove) {
        if (targetTier instanceof TierImpl) {
            TierImpl tierImpl = (TierImpl) targetTier;
            for (Annotation ann : annotationsToRemove) {
                tierImpl.removeAnnotation(ann);
            }
        }
    }

    /**
     * Performs alignment for a source annotation, creating corresponding word annotations on targetTier.
     */
    public List<Annotation> alignAndPopulate(
            File audioMedia, 
            Annotation sourceAnnotation, 
            Tier targetTier, 
            String scriptType, 
            boolean overwriteCollisions) throws Exception {

        if (audioMedia == null || !audioMedia.exists()) {
            throw new IllegalArgumentException("No valid audio media associated with this transcription");
        }
        if (sourceAnnotation == null) {
            throw new IllegalArgumentException("Source annotation cannot be null");
        }
        if (targetTier == null) {
            throw new IllegalArgumentException("Target tier cannot be null");
        }

        String transcript = sourceAnnotation.getValue();
        if (transcript == null || transcript.trim().isEmpty()) {
            throw new IllegalArgumentException("Source transcript is empty");
        }

        long tStart = sourceAnnotation.getBeginTimeBoundary();
        long tEnd = sourceAnnotation.getEndTimeBoundary();

        List<Annotation> collisions = findOverlappingAnnotations(targetTier, tStart, tEnd);
        if (!collisions.isEmpty()) {
            if (!overwriteCollisions) {
                throw new IllegalStateException("Collision detected on target tier in interval [" + tStart + ", " + tEnd + "]");
            }
            removeAnnotations(targetTier, collisions);
        }

        byte[] segmentWav = AudioSlicer.extractSegmentToWav(audioMedia, tStart, tEnd);
        AlignmentResponse response = client.alignSegment(segmentWav, transcript, scriptType);

        if (response == null || !"success".equalsIgnoreCase(response.getStatus()) || response.getWords() == null) {
            String err = (response != null && response.getError() != null) ? response.getError() : "Unknown alignment error";
            throw new RuntimeException("Alignment failed: " + err);
        }

        List<Annotation> createdAnnotations = new ArrayList<>();
        if (targetTier instanceof TierImpl) {
            TierImpl tierImpl = (TierImpl) targetTier;
            for (WordAlignment word : response.getWords()) {
                long wordAbsBegin = tStart + word.getStartMs();
                long wordAbsEnd = Math.min(tStart + word.getEndMs(), tEnd);
                
                if (wordAbsBegin < wordAbsEnd) {
                    Annotation newAnn = tierImpl.createAnnotation(wordAbsBegin, wordAbsEnd);
                    if (newAnn != null) {
                        newAnn.setValue(word.getText());
                        createdAnnotations.add(newAnn);
                    }
                }
            }
        }

        return createdAnnotations;
    }
}
