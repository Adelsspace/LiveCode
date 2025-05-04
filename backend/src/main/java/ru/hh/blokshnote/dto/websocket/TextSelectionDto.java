package ru.hh.blokshnote.dto.websocket;

public class TextSelectionDto {
  private Selection selection;
  private String username;

  public Selection getSelection() {
    return selection;
  }

  public void setSelection(Selection selection) {
    this.selection = selection;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
