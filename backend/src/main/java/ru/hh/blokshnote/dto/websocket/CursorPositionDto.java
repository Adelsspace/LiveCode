package ru.hh.blokshnote.dto.websocket;

public class CursorPositionDto {
  private PositionDto position;
  private String username;

  public PositionDto getPosition() {
    return position;
  }

  public void setPosition(PositionDto position) {
    this.position = position;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
