package ru.hh.blokshnote.dto.room.request;

import java.util.UUID;

public class CreateRoomRequest {
  private String username;
  private UUID uuid;

  public CreateRoomRequest() {
  }

  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
