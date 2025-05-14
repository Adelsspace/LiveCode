package ru.hh.blokshnote.dto.websocket;

import java.util.List;

public class TextUpdateDto {
  private List<ChangeDto> changes;
  private String username;

  public List<ChangeDto> getChanges() {
    return changes;
  }

  public void setChanges(List<ChangeDto> changes) {
    this.changes = changes;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
