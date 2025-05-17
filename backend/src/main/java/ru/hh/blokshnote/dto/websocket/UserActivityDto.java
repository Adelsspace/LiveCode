package ru.hh.blokshnote.dto.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserActivityDto {
  private boolean isActive;
  private String username;

  @JsonProperty(value="isActive")
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
