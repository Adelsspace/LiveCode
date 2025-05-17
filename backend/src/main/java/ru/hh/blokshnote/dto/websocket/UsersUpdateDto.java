package ru.hh.blokshnote.dto.websocket;

import java.util.List;

public class UsersUpdateDto {
  private List<UserStateDto> usersStates;

  public UsersUpdateDto() {
  }

  public UsersUpdateDto(List<UserStateDto> usersStates) {
    this.usersStates = usersStates;
  }

  public List<UserStateDto> getUsersStates() {
    return usersStates;
  }

  public void setUsersStates(List<UserStateDto> usersStates) {
    this.usersStates = usersStates;
  }
}
