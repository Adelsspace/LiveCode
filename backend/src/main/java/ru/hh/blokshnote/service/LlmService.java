package ru.hh.blokshnote.service;

import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class LlmService {
  private final DeepSeekChatModel chatModel;

  public LlmService(DeepSeekChatModel chatModel) {
    this.chatModel = chatModel;
  }

  @Async
  public CompletableFuture<String> getReviewResponseAsync(String editorText, String prompt) {
    if (prompt.isBlank()) {
      prompt = "Read the problem. Analyze the solution and give suggestions.";
    }

    String combined = prompt + "\n\n" + editorText;
    String response = chatModel.call(combined);
    return CompletableFuture.completedFuture(response);
  }
}
