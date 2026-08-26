package org.cherokee.elan.client;

import com.google.gson.Gson;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.cherokee.elan.model.AlignmentResponse;

import java.io.IOException;

public class AlignmentClient {

    private final String backendBaseUrl;
    private final Gson gson = new Gson();

    public AlignmentClient() {
        this("http://localhost:5050");
    }

    public AlignmentClient(String backendBaseUrl) {
        this.backendBaseUrl = backendBaseUrl.endsWith("/") 
            ? backendBaseUrl.substring(0, backendBaseUrl.length() - 1) 
            : backendBaseUrl;
    }

    public AlignmentResponse alignSegment(byte[] wavBytes, String transcript, String scriptType) throws IOException {
        String endpoint = backendBaseUrl + "/v1/align/segment";
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(endpoint);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("audio", wavBytes, ContentType.create("audio/wav"), "segment.wav");
            builder.addTextBody("transcript", transcript, ContentType.create("text/plain", java.nio.charset.StandardCharsets.UTF_8));
            builder.addTextBody("script_type", scriptType != null ? scriptType : "syllabary", ContentType.create("text/plain", java.nio.charset.StandardCharsets.UTF_8));
            
            post.setEntity(builder.build());

            return httpClient.execute(post, response -> {
                int statusCode = response.getCode();
                String body = response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : "";
                if (statusCode >= 200 && statusCode < 300) {
                    return gson.fromJson(body, AlignmentResponse.class);
                } else {
                    throw new IOException("Alignment server returned error (HTTP " + statusCode + "): " + body);
                }
            });
        }
    }
}
