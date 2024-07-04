package com.springoot.vsubclone.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class VideoGenerationService {

    private final String apiKey;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public VideoGenerationService(@Value("${openai.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public String generateScriptFromPrompt(String prompt) throws IOException {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("model", "gpt-3.5-turbo");
        jsonMap.put("messages", new Object[]{
                new HashMap<String, String>() {{
                    put("role", "user");
                    put("content", prompt);
                }}
        });

        String json = objectMapper.writeValueAsString(jsonMap);

        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.get("choices").get(0).get("message").get("content").asText();
        }
    }

    public String generateVideoFromScript(String script) throws IOException, InterruptedException {
        String scriptText = "Video Script:\n" + script;
        File scriptFile = new File("script.txt");
        java.nio.file.Files.writeString(scriptFile.toPath(), scriptText);

        File videoFile = new File("generated_video.mp4");
        String ffmpegCommand = String.format("/Users/syednoumanalishah/anaconda3/bin/ffmpeg -f lavfi -i color=c=blue:s=320x240:d=10 -vf \"drawtext=fontfile=/path/to/font.ttf: textfile=script.txt: fontsize=24: fontcolor=white: x=(w-text_w)/2: y=(h-text_h)/2\" -y %s",
                videoFile.getAbsolutePath());

        ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", ffmpegCommand);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        process.waitFor();

        return videoFile.getAbsolutePath();
    }
}