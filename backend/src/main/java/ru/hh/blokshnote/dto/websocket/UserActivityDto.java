package ru.hh.blokshnote.dto.websocket;

public class UserActivityDto {
  private boolean isActive;
  private String username;

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
