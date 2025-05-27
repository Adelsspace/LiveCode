package ru.hh.blokshnote.service;

import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class LlmService {
  @Value("${spring.ai.deepseek.api-key}")
  private String apiKey;

  private final DeepSeekChatModel chatModel;
  private static final String PLACEHOLDER_KEY = "no-key";
  private static final String PLACEHOLDER_RESPONSE =
      "LLM review are disabled, since no llm key is provided";

  public LlmService(DeepSeekChatModel chatModel) {
    this.chatModel = chatModel;
  }

  @Async
  public CompletableFuture<String> getReviewResponseAsync(String editorText, String prompt) {
    if (prompt == null || prompt.isBlank()) {
      prompt = "Read the problem. Analyze the solution and give suggestions.";
    }
    if (PLACEHOLDER_KEY.equals(apiKey.trim())) {
      return CompletableFuture.completedFuture(PLACEHOLDER_RESPONSE);
    }

    String combined = prompt + "\n\n" + editorText;
    String response = chatModel.call(combined);
    return CompletableFuture.completedFuture(response);
  }
}
