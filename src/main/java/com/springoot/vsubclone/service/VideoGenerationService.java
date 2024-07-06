package com.springoot.vsubclone.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class VideoGenerationService {

    private final String apiKey;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private static final HttpClient httpClient = HttpClient.newHttpClient();

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
            //print response from the API
            System.out.println(jsonNode.get("choices").get(0).get("message").get("content").asText());
            //save to a file named script.txt
            java.nio.file.Files.writeString(new File("script.txt").toPath(), jsonNode.get("choices").get(0).get("message").get("content").asText());
            return jsonNode.get("choices").get(0).get("message").get("content").asText();
        }
    }
    //a function to only extract text from [] in the script
    public String extractFromScript(String script) {
        StringBuilder extractedText = new StringBuilder();
        StringBuilder extractedVoice = new StringBuilder();
        for (String line : script.split("\n")) {
            if (line.startsWith("[")) {
                extractedText.append(line).append(",");
            }
            else if (line.startsWith("Voiceover:") || line.startsWith("Narrator:") || line.startsWith("Narrator :")){
                //remove "Voiceover:" and "Narrator:" from the line
                line = line.replace("Voiceover:", "");
                line = line.replace("Narrator:", "");
                line = line.replace("Narrator :", "");
                extractedVoice.append(line).append(" ");
            }
        }
        //save text to a file named img-prompts.txt
        try {
            java.nio.file.Files.writeString(new File("img-prompts.txt").toPath(), extractedText.toString());
            java.nio.file.Files.writeString(new File("voice-prompts.txt").toPath(), extractedVoice.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //print the extracted text
        System.out.println(extractedText.toString());
        System.out.println(extractedVoice.toString());
        return extractedText.toString();

    }
    //generate voice over from voice-prompts.txt using text to speech API
    public String generateVoiceOverFromScript() throws IOException {
        String script = "";
        try {
            script = Files.readString(Paths.get("voice-prompts.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Script read from file:\n" + script);
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("model", "tts-1");
        jsonMap.put("input", script);
        jsonMap.put("voice", "alloy");

        String json = objectMapper.writeValueAsString(jsonMap);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/audio/speech"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new IOException("Unexpected response code: " + response.statusCode());
            }
            byte[] audioBytes = response.body();
            File audioFile = new File("voice_over.mp3");
            try (FileOutputStream fileOutputStream = new FileOutputStream(audioFile)) {
                fileOutputStream.write(audioBytes);
            }
            return audioFile.getAbsolutePath();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
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
    //generate video from script.txt, voice-over.wav and image.png using DALL.E API
    public String generateVideoFromScript(String script) throws IOException, InterruptedException {
        // Write script text to script.txt
        String scriptText = "Video Script:\n" + script;
        File scriptFile = new File("script.txt");
        java.nio.file.Files.writeString(scriptFile.toPath(), scriptText);

        // Define ffmpeg command
        String ffmpegCommand = "C:/Users/raahi/anaconda3/envs/ffmpeg-env/Library/bin/ffmpeg -loop 1 -i generated_image.png -i voice_over.mp3 -filter_complex \"[0:v]scale=1280:720,setsar=1[v];[v]drawtext=fontfile=/path/to/font.ttf:textfile=script.txt:fontsize=24:fontcolor=white:box=1:boxcolor=black@0.5:boxborderw=5:x=(w-text_w)/2:y=(h-text_h)/2[out]\" -map \"[out]\" -map 1:a -c:v libx264 -preset medium -crf 23 -c:a aac -b:a 192k -shortest -y generated_video.mp4";

        // Start ffmpeg process
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("cmd", ffmpegCommand);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        // Optionally, set a timeout (2 minutes in this case)
        boolean completed = process.waitFor(2, java.util.concurrent.TimeUnit.MINUTES);
        if (!completed) {
            // Process took too long; terminate it
            process.destroy();
            throw new InterruptedException("FFmpeg process timed out");
        }

        // Check if the process exited with an error
        int exitCode = process.exitValue();
        if (exitCode != 0) {
            // Handle error case
            throw new IOException("FFmpeg process returned non-zero exit code: " + exitCode);
        }

        return new File("generated_video.mp4").getAbsolutePath();
    }
}