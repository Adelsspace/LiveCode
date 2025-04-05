package ru.hh.blokshnote.dto.responses;

import java.util.UUID;

public class RoomAdminDto {
  private final UUID roomUuid;
  private final String adminToken;

  public RoomAdminDto(UUID roomUuid, String adminToken) {
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
