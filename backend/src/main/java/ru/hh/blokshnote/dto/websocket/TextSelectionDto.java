package ru.hh.blokshnote.dto.websocket;

public class TextSelectionDto {
  private SelectionDto selection;
  private String username;

  public SelectionDto getSelection() {
    return selection;
  }

  public void setSelection(SelectionDto selection) {
    this.selection = selection;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
