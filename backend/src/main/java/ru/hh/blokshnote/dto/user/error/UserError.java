package ru.hh.blokshnote.dto.user.error;

public class UserError {
  private final String error;


  public UserError(String error) {
    this.error = error;
  }

  public String getError() {
    return error;
  }
}
