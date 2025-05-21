package ru.hh.blokshnote.dto.websocket;

public class LanguageChangeDto {
  private String language;
  private String username;

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
