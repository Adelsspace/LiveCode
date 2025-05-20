package ru.hh.blokshnote.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class LlmService {
  @Async
  public CompletableFuture<String> getReviewResponseAsync(String editorText, String prompt) {
    String response = "LLM response";
    return CompletableFuture.completedFuture(response);
  }
}
