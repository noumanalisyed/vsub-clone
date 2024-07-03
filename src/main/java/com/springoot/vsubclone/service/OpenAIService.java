package com.springoot.vsubclone.service;

import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import com.theokanning.openai.service.OpenAiService;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.logging.Logger;
@Service
public class OpenAIService {

    private final OpenAiService openAiService;

    private static final Logger logger = Logger.getLogger(OpenAIService.class.getName());

    public OpenAIService(@Value("${openai.api.key}") String apiKey) {
        this.openAiService = new OpenAiService(apiKey);
    }

    public String generateScript(String prompt) {
        logger.info("Generating script for prompt: " + prompt);

        CompletionRequest completionRequest = CompletionRequest.builder()
                .prompt(prompt)
                .model("gpt-3.5-turbo-instruct")
                .maxTokens(500)
                .build();

        CompletionResult completionResult = openAiService.createCompletion(completionRequest);
        String script = completionResult.getChoices().get(0).getText().trim();

        logger.info("Generated script: " + script);
        return script;
    }
}

