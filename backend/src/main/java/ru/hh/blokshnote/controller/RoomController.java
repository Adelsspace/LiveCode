package ru.hh.blokshnote.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hh.blokshnote.dto.requests.CreateRoomRequest;
import ru.hh.blokshnote.dto.requests.CreateUserRequest;
import ru.hh.blokshnote.dto.responses.RoomAdminDto;
import ru.hh.blokshnote.dto.responses.RoomDto;
import ru.hh.blokshnote.dto.responses.UserDto;
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
  public ResponseEntity<RoomAdminDto> createRoom(@RequestBody CreateRoomRequest request) {
    Room createdRoom = roomService.createRoomWithAdmin(request);
    return ResponseEntity.ok(new RoomAdminDto(createdRoom.getRoomUuid(), createdRoom.getAdminToken()));
  }

  @GetMapping("/{uuid}")
  public ResponseEntity<RoomDto> getRoom(@PathVariable("uuid") UUID roomUuid) {
    Room room = roomService.getRoomByUuid(roomUuid);
    if (room == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(new RoomDto(room.getRoomUuid()));
  }

  @PostMapping("/{uid}/users")
  public ResponseEntity<?> addUser(@PathVariable("uid") UUID roomUuid,
                                   @RequestBody CreateUserRequest request) {
    try {
      User createdUser = roomService.addUserToRoom(roomUuid, request);
      return ResponseEntity.ok(new UserDto(
          createdUser.getName(),
          createdUser.isAdmin(),
          createdUser.getRoom().getRoomUuid()));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
  }
}
