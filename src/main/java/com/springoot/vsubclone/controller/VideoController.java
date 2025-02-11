package com.springoot.vsubclone.controller;

import com.springoot.vsubclone.controller.request.PromptRequest;
import com.springoot.vsubclone.controller.response.VideoResponse;
import com.springoot.vsubclone.service.VideoGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class VideoController {

    @Autowired
    private VideoGenerationService videoGenerationService;
    @Autowired
    private StringHttpMessageConverter stringHttpMessageConverter;

    @PostMapping("/generate_video")
    public VideoResponse generateVideo(@RequestBody PromptRequest request) {
        try {
            System.out.println(request.getPrompt());
            String script = videoGenerationService.generateScriptFromPrompt(request.getPrompt());
            String img_prompt= videoGenerationService.extractFromScript(script);
            String voiceover_path = videoGenerationService.generateVoiceOverFromScript();
            String img_path = videoGenerationService.generateImageFromScript(img_prompt);
            System.out.println("img generated at: " + img_path);
            System.out.println("voice-over generated at: " + voiceover_path);
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
