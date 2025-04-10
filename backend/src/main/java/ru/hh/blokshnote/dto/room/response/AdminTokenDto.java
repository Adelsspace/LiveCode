package ru.hh.blokshnote.dto.room.response;

import java.util.UUID;

public class AdminTokenDto {
  private final UUID adminToken;

  public AdminTokenDto(UUID adminToken) {
    this.adminToken = adminToken;
  }

  public UUID getAdminToken() {
    return adminToken;
  }
}
