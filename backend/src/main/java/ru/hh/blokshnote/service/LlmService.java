package ru.hh.blokshnote.service;

import org.springframework.stereotype.Service;

@Service
public class LlmService {

  public LlmService() {
  }

  public String getReviewResponse(String editorText, String prompt) {
    return "LLM response";
  }
}
