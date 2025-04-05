package ru.hh.blokshnote.dto.responses;

import java.util.UUID;

public class RoomDto {
  private UUID roomUuid;

  public RoomDto(UUID roomUuid) {
    this.roomUuid = roomUuid;
  }

  public UUID getRoomUuid() {
    return roomUuid;
  }

  public void setRoomUuid(UUID roomUuid) {
    this.roomUuid = roomUuid;
  }
}
