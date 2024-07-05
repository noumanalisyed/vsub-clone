package com.springoot.vsubclone.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    //a function to only extract text from [] in the script
    public String extractTextFromScript(String script) {
        StringBuilder extractedText = new StringBuilder();
        for (String line : script.split("\n")) {
            if (line.startsWith("[")) {
                extractedText.append(line).append(",");
            }
        }
        //save text to a file named img-prompts.txt
        try {
            java.nio.file.Files.writeString(new File("img-prompts.txt").toPath(), extractedText.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //print the extracted text
        System.out.println(extractedText.toString());
        return extractedText.toString();
    }
    //generate images from script.txt using DALL.E API
    public String generateImageFromScript(String script) throws IOException {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("prompt", "make an stylistic image that suits the topic : "+ script + "realistic and detailed and high quality image.");
        jsonMap.put("n", 1);
        jsonMap.put("size", "512x512");

        String json = objectMapper.writeValueAsString(jsonMap);

        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/images/generations")
                .header("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String imageUrl = jsonNode.get("data").get(0).get("url").asText();

            Request imageRequest = new Request.Builder().url(imageUrl).build();
            try (Response imageResponse = client.newCall(imageRequest).execute()) {
                if (!imageResponse.isSuccessful()) {
                    throw new IOException("Unexpected code " + imageResponse);
                }
                InputStream imageStream = imageResponse.body().byteStream();
                File imageFile = new File("generated_image.png");
                try (FileOutputStream fileOutputStream = new FileOutputStream(imageFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = imageStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                }
                return imageFile.getAbsolutePath();
            }
        }
    }

    public String generateVideoFromScript(String script) throws IOException, InterruptedException {
        String scriptText = "Video Script:\n" + script;
        File scriptFile = new File("script.txt");
        java.nio.file.Files.writeString(scriptFile.toPath(), scriptText);

        File videoFile = new File("generated_video.mp4");
        //use the generated_imagepng to create a video with the script text
        String ffmpegCommand = String.format("C:/Users/raahi/anaconda3/envs/ffmpeg-env/Library/bin/ffmpeg -loop 1 -i generated_image.png -t 10 -vf \"drawtext=fontfile=/path/to/font.ttf: textfile=script.txt: fontsize=8: fontcolor=white: x=(w-text_w)/2: y=(h-text_h)/2\" -y %s",
              videoFile.getAbsolutePath());

        ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", ffmpegCommand);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        process.waitFor();

        return videoFile.getAbsolutePath();
    }
}