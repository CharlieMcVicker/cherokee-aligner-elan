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

    public String getStatus() {
        return status;
    }

    public String getScriptType() {
        return scriptType;
    }

    public List<WordAlignment> getWords() {
        return words;
    }

    public String getError() {
        return error;
    }
}
