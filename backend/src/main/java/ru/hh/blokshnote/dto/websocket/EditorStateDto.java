package ru.hh.blokshnote.dto.websocket;

public class EditorStateDto {
  private String text;
  private String language;

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }
}
