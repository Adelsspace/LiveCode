package ru.hh.blokshnote.mapper;

import ru.hh.blokshnote.dto.websocket.UserState;
import ru.hh.blokshnote.dto.websocket.UserStateDto;
import ru.hh.blokshnote.entity.User;

public class UserStateMapper {

  public static UserStateDto toDto(UserState userState) {
    UserStateDto userStateDto = new UserStateDto();
    userStateDto.setUsername(userState.getUsername());
    userStateDto.setAdmin(userState.isAdmin());
    userStateDto.setColor(userState.getColor());
    return userStateDto;
  }

  public static UserStateDto toDto(User user) {
    UserStateDto userStateDto = new UserStateDto();
    userStateDto.setUsername(user.getName());
    userStateDto.setAdmin(user.isAdmin());
    userStateDto.setColor(user.getColor());
    return userStateDto;
  }
}
