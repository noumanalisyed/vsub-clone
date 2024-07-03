package com.springoot.vsubclone.controller;

import com.springoot.vsubclone.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    private static final Logger logger = Logger.getLogger(VideoController.class.getName());

    @Autowired
    private OpenAIService openAIService;

    @PostMapping("/generate_video")
    public ResponseEntity<String> generateVideo(@RequestBody Map<String, String> payload) {
        String prompt = payload.get("prompt");
        String script = openAIService.generateScript(prompt);
        String videoPath = createVideoFromScript(script);
        return ResponseEntity.ok(videoPath);
    }

    @PostMapping("/upload_video")
    public ResponseEntity<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        String uploadDirectory = "uploads/";
        File uploadDir = new File(uploadDirectory);

        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        try {
            file.transferTo(new File(uploadDirectory + file.getOriginalFilename()));
            return new ResponseEntity<>("File uploaded successfully", HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("File upload failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String createVideoFromScript(String script) {
        // Placeholder for video creation logic
        String videoFileName = "generated_video.mp4";
        Path videoPath = Paths.get("uploads/" + videoFileName);

        try {
            Files.createDirectories(videoPath.getParent());
            Files.write(videoPath, script.getBytes()); // Replace with actual video creation logic
        } catch (IOException e) {
            e.printStackTrace();
        }

        return videoPath.toString();
    }
}
