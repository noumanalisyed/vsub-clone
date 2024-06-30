package com.springoot.vsubclone.controller;

import com.springoot.vsubclone.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.logging.Logger;
@RestController
@RequestMapping("/api")
public class VideoController {

    private static final Logger logger = Logger.getLogger(VideoController.class.getName());

    @Autowired
    private OpenAIService openAIService;

    @PostMapping("/generate_video")
    public ResponseEntity<String> generateVideo(@RequestBody Map<String, String> payload) {
        logger.info("Received request to generate video with payload: " + payload);

        String prompt = payload.get("prompt");
        logger.info("Prompt: " + prompt);

        String script = openAIService.generateScript(prompt);
        logger.info("Generated script: " + script);

        String videoUrl = createVideoFromScript(script);
        logger.info("Video URL: " + videoUrl);

        return ResponseEntity.ok(videoUrl);
    }

    private String createVideoFromScript(String script) {
        // Placeholder for video creation logic
        return "path/to/generated/video.mp4";
    }
}

