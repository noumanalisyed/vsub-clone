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
            //print request prompt
//            System.out.println(request.getPrompt());
//            String script = videoGenerationService.generateScriptFromPrompt(request.getPrompt());
//            String img_prompt = videoGenerationService.extractTextFromScript(script);
//            String img_path = videoGenerationService.generateImageFromScript(img_prompt);
//            System.out.println("img generated at: " + img_path);
            String videoPath = videoGenerationService.generateVideoFromScript("Video Script:\n" +
                    "Title: Larry the Cat\n" +
                    "\n" +
                    "[Intro shot of a cute orange tabby cat lounging in front of a famous government building]\n" +
                    "\n" +
                    "Narrator: Meet Larry the Cat, the beloved resident feline of 10 Downing Street in London.\n" +
                    "\n" +
                    "[Cut to Larry playing with a ball of yarn in the Prime Minister's office]\n" +
                    "\n" +
                    "Narrator: Larry may act like your typical playful cat, but don't let his cute appearance fool you. He's also a fierce mouser, keeping the building free of any unwanted pests.\n" +
                    "\n" +
                    "[Cut to Larry lounging with a group of high-profile politicians]\n" +
                    "\n" +
                    "Politician 1: Larry, what do you think about the latest Brexit negotiations?\n" +
                    "\n" +
                    "Larry: [meows and rolls over for a belly rub]\n" +
                    "\n" +
                    "[Everyone laughs]\n" +
                    "\n" +
                    "Narrator: Larry is not just a working cat, he's also a diplomat, bringing together people from different political parties with his charming personality.\n" +
                    "\n" +
                    "[Cut to Larry chasing a squirrel in the garden]\n" +
                    "\n" +
                    "Narrator: Larry may have his hands full with all the responsibilities that come with being Downing Street's official cat, but he always finds time to enjoy the simple pleasures in life, like chasing squirrels in the garden.\n" +
                    "\n" +
                    "[Cut to Larry falling asleep on the Prime Minister's desk]\n" +
                    "\n" +
                    "Narrator: Despite his busy schedule, Larry always manages to find a cozy spot to take a cat nap. After all, even the hardest-working cats need their beauty sleep.\n" +
                    "\n" +
                    "[Cut to Larry being petted by an admiring group of tourists]\n" +
                    "\n" +
                    "Narrator: Larry may just be a cat, but to the people of London, he's a symbol of unity and companionship in the heart of government.\n" +
                    "\n" +
                    "[Closing shot of Larry strolling through the streets of London, with the iconic Big Ben in the background]\n" +
                    "\n" +
                    "Narrator: So next time you visit 10 Downing Street, be sure to say hello to Larry the Cat, the purr-fect ambassador of charm, grace, and diplomacy.\n" +
                    "\n" +
                    "[End credits]");
            return new VideoResponse(videoPath);
        } catch (IOException e) {
            e.printStackTrace();
            return new VideoResponse("Error generating video: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
