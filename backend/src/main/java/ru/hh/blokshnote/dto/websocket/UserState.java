package ru.hh.blokshnote.dto.websocket;

public class UserState {
  private Long id;
  private String username;
  private boolean isAdmin;
  private String color;

  public UserState(Long id, String username, boolean isAdmin, String color) {
    this.id = id;
    this.username = username;
    this.isAdmin = isAdmin;
    this.color = color;
  }

  public UserState() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public boolean isAdmin() {
    return isAdmin;
  }

  public void setAdmin(boolean admin) {
    isAdmin = admin;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }
}
