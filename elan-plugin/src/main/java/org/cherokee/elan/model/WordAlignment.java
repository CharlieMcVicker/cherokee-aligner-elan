package org.cherokee.elan.model;

import com.google.gson.annotations.SerializedName;

public class WordAlignment {
    @SerializedName("text")
    private String text;

    @SerializedName("start_ms")
    private long startMs;

    @SerializedName("end_ms")
    private long endMs;

    @SerializedName("confidence")
    private double confidence;

    public WordAlignment() {}

    public WordAlignment(String text, long startMs, long endMs, double confidence) {
        this.text = text;
        this.startMs = startMs;
        this.endMs = endMs;
        this.confidence = confidence;
    }

    public String getText() {
        return text;
    }

    public long getStartMs() {
        return startMs;
    }

    public long getEndMs() {
        return endMs;
    }

    public double getConfidence() {
        return confidence;
    }
}
