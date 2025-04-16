package ru.hh.blokshnote.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.hh.blokshnote.dto.room.request.CreateRoomRequest;
import ru.hh.blokshnote.dto.room.response.AdminTokenDto;
import ru.hh.blokshnote.dto.room.response.RoomDto;
import ru.hh.blokshnote.dto.room.response.WebSocketUrlDto;
import ru.hh.blokshnote.dto.user.request.CreateUserRequest;
import ru.hh.blokshnote.dto.user.response.UserDto;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.entity.User;
import ru.hh.blokshnote.service.RoomService;

import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

  private static final Logger LOGGER = LoggerFactory.getLogger(RoomController.class);

  private final RoomService roomService;

  public RoomController(RoomService roomService) {
    this.roomService = roomService;
  }

  @PostMapping
  public AdminTokenDto createRoom(@RequestBody CreateRoomRequest request) {
    Room createdRoom = roomService.createRoomWithAdmin(request);
    return new AdminTokenDto(createdRoom.getAdminToken());
  }

  @GetMapping("/{uuid}")
  public RoomDto getRoom(@PathVariable("uuid") UUID roomUuid) {
    Room room = roomService.getRoomByUuid(roomUuid);
    return new RoomDto(room.getRoomUuid(), "ok");
  }

  @PostMapping("/{uuid}/users")
  public UserDto addUser(
      @PathVariable("uuid") UUID roomUuid,
      @RequestBody CreateUserRequest request
  ) {
    User createdUser = roomService.addUserToRoom(roomUuid, request);
    return new UserDto(
        createdUser.getName(),
        createdUser.isAdmin(),
        createdUser.getRoom().getRoomUuid()
    );
  }

  @PostMapping("/{uuid}/admin")
  public UserDto addAdmin(
      @PathVariable("uuid") UUID roomUuid,
      @RequestParam("adminToken") UUID adminToken,
      @RequestBody CreateUserRequest request
  ) {
    User createdAdmin = roomService.addAdminToRoom(roomUuid, adminToken, request);
    return new UserDto(
        createdAdmin.getName(),
        createdAdmin.isAdmin(),
        createdAdmin.getRoom().getRoomUuid()
    );
  }

  @GetMapping("/{uuid}/url")
  public WebSocketUrlDto getWebSocketUrl(@PathVariable("uuid") UUID roomUuid, HttpServletRequest request) {
    LOGGER.info("Request websocket URL for roomUuid={}", roomUuid);
    return roomService.getRoomUrl(roomUuid, request.getServerName(), request.getServerPort(), request.getScheme());
  }
}
