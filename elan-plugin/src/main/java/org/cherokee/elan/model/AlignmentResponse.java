package org.cherokee.elan.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AlignmentResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("script_type")
    private String scriptType;

    @SerializedName("words")
    private List<WordAlignment> words;

    @SerializedName("error")
    private String error;

    public AlignmentResponse() {
    }

    public AlignmentResponse(String status, String scriptType, List<WordAlignment> words, String error) {
        this.status = status;
        this.scriptType = scriptType;
        this.words = words;
        this.error = error;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getScriptType() {
        return scriptType;
    }

    public void setScriptType(String scriptType) {
        this.scriptType = scriptType;
    }

    public List<WordAlignment> getWords() {
        return words;
    }

    public void setWords(List<WordAlignment> words) {
        this.words = words;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
