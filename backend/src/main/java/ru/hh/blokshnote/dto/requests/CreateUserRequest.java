package ru.hh.blokshnote.dto.requests;


public class CreateUserRequest {
  private String username;

  public CreateUserRequest() {
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
