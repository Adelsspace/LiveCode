package ru.hh.blokshnote.dto.room.error;

public class RoomError {
  private final String message;

  public RoomError(String message) {
    this.message = message;
  }


  public String getError() {
    return message;
  }
}
