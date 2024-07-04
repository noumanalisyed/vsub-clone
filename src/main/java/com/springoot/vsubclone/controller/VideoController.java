package com.springoot.vsubclone.controller;

import com.springoot.vsubclone.controller.request.PromptRequest;
import com.springoot.vsubclone.controller.response.VideoResponse;
import com.springoot.vsubclone.service.VideoGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class VideoController {

    @Autowired
    private VideoGenerationService videoGenerationService;

    @PostMapping("/generate_video")
    public VideoResponse generateVideo(@RequestBody PromptRequest request) {
        try {
            String script = videoGenerationService.generateScriptFromPrompt(request.getPrompt());
            String videoPath = videoGenerationService.generateVideoFromScript(script);
            return new VideoResponse(videoPath);
        } catch (IOException e) {
            e.printStackTrace();
            return new VideoResponse("Error generating video: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
