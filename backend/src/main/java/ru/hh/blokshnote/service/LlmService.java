package ru.hh.blokshnote.service;

import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class LlmService {
  @Value("${spring.ai.deepseek.api-key}")
  private String apiKey;

  private final DeepSeekChatModel chatModel;
  private static final String PLACEHOLDER_KEY = "no-key";
  private static final String PLACEHOLDER_RESPONSE = "LLM reviews are disabled since no llm key is provided";

  private static final long TIMEOUT_SECONDS = 20L;
  private static final String TIMEOUT_RESPONSE = "Нейросеть занята, попробуйте еще раз";


  private static final String PLACEHOLDER_PROMPT = "Analyze the solution to the given problem.";
  private static final String INSTRUCTIONS = """
      Keep the analysis clear and short.
      Provide the time and space complexities of the given code and suggest improvements.
      You are allowed to use 20 sentences max. Do not use markdown. Provide the response in Russian.
      """;

  public LlmService(DeepSeekChatModel chatModel) {
    this.chatModel = chatModel;
  }

  @Async
  public CompletableFuture<String> getReviewResponseAsync(String editorText, String prompt) {
    if (prompt == null || prompt.isBlank()) {
      prompt = PLACEHOLDER_PROMPT;
    }

    if (PLACEHOLDER_KEY.equals(apiKey.trim())) {
      return CompletableFuture.completedFuture(PLACEHOLDER_RESPONSE);
    }

    String combined = prompt + "\n" + INSTRUCTIONS + "\n\n" + editorText;
    return CompletableFuture
        .supplyAsync(() -> chatModel.call(combined))
        .orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .exceptionally(ex -> TIMEOUT_RESPONSE);
  }
}
