package ru.hh.blokshnote.dto.room.request;

import java.util.UUID;

public class CreateRoomRequest {
  private UUID uuid;

  public CreateRoomRequest() {
  }

  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

}
