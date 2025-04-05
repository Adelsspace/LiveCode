package ru.hh.blokshnote.dto.requests;

public class CreateRoomRequest {
  private String username;

  public CreateRoomRequest() {
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}

