package ru.hh.blokshnote.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hh.blokshnote.dto.room.error.RoomError;
import ru.hh.blokshnote.dto.room.request.CreateRoomRequest;
import ru.hh.blokshnote.dto.user.error.UserError;
import ru.hh.blokshnote.dto.user.request.CreateUserRequest;
import ru.hh.blokshnote.dto.room.response.RoomWithAdminDto;
import ru.hh.blokshnote.dto.room.response.RoomDto;
import ru.hh.blokshnote.dto.user.response.UserDto;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.entity.User;
import ru.hh.blokshnote.service.RoomService;

import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

  private final RoomService roomService;

  public RoomController(RoomService roomService) {
    this.roomService = roomService;
  }

  @PostMapping
  public ResponseEntity<RoomWithAdminDto> createRoom(@RequestBody CreateRoomRequest request) {
    Room createdRoom = roomService.createRoomWithAdmin(request);
    return ResponseEntity.ok(new RoomWithAdminDto(createdRoom.getRoomUuid(), createdRoom.getAdminToken()));
  }

  @GetMapping("/{uuid}")
  public ResponseEntity<?> getRoom(@PathVariable("uuid") UUID roomUuid) {
    Room room = roomService.getRoomByUuid(roomUuid);
    if (room == null) {
      RoomError error = new RoomError("Room does not exist");
      return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    return ResponseEntity.ok(new RoomDto(room.getRoomUuid()));
  }

  @PostMapping("/{uuid}/users")
  public ResponseEntity<?> addUser(@PathVariable("uuid") UUID roomUuid,
                                   @RequestBody CreateUserRequest request) {
    try {
      User createdUser = roomService.addUserToRoom(roomUuid, request);
      return ResponseEntity.ok(new UserDto(
          createdUser.getName(),
          createdUser.isAdmin(),
          createdUser.getRoom().getRoomUuid()));
    } catch (IllegalArgumentException e) {
      RoomError error = new RoomError("Room does not exist");
      return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    } catch (IllegalStateException e) {
      UserError error = new UserError("User with this name already exists");
      return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
  }
}
