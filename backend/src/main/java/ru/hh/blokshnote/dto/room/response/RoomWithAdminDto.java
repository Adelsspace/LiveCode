package ru.hh.blokshnote.dto.room.response;

import java.util.UUID;

public class RoomWithAdminDto {
  private final UUID roomUuid;
  private final String adminToken;

  public RoomWithAdminDto(UUID roomUuid, String adminToken) {
    this.roomUuid = roomUuid;
    this.adminToken = adminToken;
  }

  public UUID getRoomUuid() {
    return roomUuid;
  }

  public String getAdminToken() {
    return adminToken;
  }
}
