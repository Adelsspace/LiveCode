package ru.hh.blokshnote.dto.responses;

import java.util.UUID;

public class UserDto {
  private final String name;
  private final boolean isAdmin;
  private final RoomDto room;

  public UserDto(String name, boolean isAdmin, UUID roomUuid) {
    this.name = name;
    this.isAdmin = isAdmin;
    this.room = new RoomDto(roomUuid);
  }

  public String getName() {
    return name;
  }

  public boolean isAdmin() {
    return isAdmin;
  }

  public RoomDto getRoom() {
    return room;
  }
}