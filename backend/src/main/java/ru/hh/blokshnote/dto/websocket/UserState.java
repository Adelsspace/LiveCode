package ru.hh.blokshnote.dto.websocket;

public class UserState {
  private String username;
  private boolean isActive;
  private boolean isAdmin;

  public UserState(String username, boolean isActive, boolean isAdmin) {
    this.username = username;
    this.isActive = isActive;
    this.isAdmin = isAdmin;
  }

  public UserState() {
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }

  public boolean isAdmin() {
    return isAdmin;
  }

  public void setAdmin(boolean admin) {
    isAdmin = admin;
  }
}