package ru.hh.blokshnote.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.blokshnote.config.WebSocketConfig;
import ru.hh.blokshnote.dto.room.request.CreateRoomRequest;
import ru.hh.blokshnote.dto.room.response.WebSocketUrlDto;
import ru.hh.blokshnote.dto.user.request.CreateUserRequest;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.entity.User;
import ru.hh.blokshnote.repository.RoomRepository;
import ru.hh.blokshnote.repository.UserRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RoomService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RoomService.class);

  private static final Duration ROOM_TIME_TO_LIVE = Duration.ofHours(3);

  @Value("${server.port}")
  private int serverPort;
  @Value("${server.host}")
  private String serverHost;

  private final RoomRepository roomRepository;
  private final UserRepository userRepository;

  public RoomService(RoomRepository roomRepository, UserRepository userRepository) {
    this.roomRepository = roomRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public Room createRoomWithAdmin(CreateRoomRequest request) {
    UUID roomUuid = request.getUuid();

    Optional<Room> existingRoom = roomRepository.findByRoomUuid(roomUuid);
    if (existingRoom.isPresent()) {
      return existingRoom.get();
    }

    Instant now = Instant.now();
    Room room = new Room();
    room.setRoomUuid(roomUuid);
    room.setAdminToken(UUID.randomUUID());
    room.setCreatedAt(now);
    room.setExpiredAt(now.plus(ROOM_TIME_TO_LIVE));
    room.setEditorText("");
    room = roomRepository.save(room);

    User adminUser = new User();
    adminUser.setName(request.getUsername());
    adminUser.setAdmin(true);
    adminUser.setRoom(room);
    userRepository.save(adminUser);

    return room;
  }


  @Transactional
  public User addUserToRoom(UUID roomUuid, CreateUserRequest request) {
    Room room = getRoomByUuid(roomUuid);

    userRepository.findByNameAndRoom(request.getUsername(), room)
        .ifPresent(user -> {
          throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this name already exists in the room");
        });

    User user = new User();
    user.setName(request.getUsername());
    user.setAdmin(false);
    user.setRoom(room);
    return userRepository.save(user);
  }

  @Transactional(readOnly = true)
  public Room getRoomByUuid(UUID roomUuid) {
    return roomRepository.findByRoomUuid(roomUuid)
        .orElseThrow(() -> {
          LOGGER.info("Room with UUID={} not found", roomUuid);
          return new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Room with UUID=%s not found", roomUuid));
        });
  }

  @Transactional(readOnly = true)
  public WebSocketUrlDto getRoomUrl(UUID roomUuid) {
    roomRepository.findByRoomUuid(roomUuid)
        .orElseThrow(() -> {
          LOGGER.info("Room with UUID={} not found", roomUuid);
          return new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Room with UUID=%s not found", roomUuid));
        });
    return new WebSocketUrlDto(String.format("ws://%s:%d%s", serverHost, serverPort, WebSocketConfig.ROOM_URI_TEMPLATE));
  }

  @Transactional
  public Room updateEditorText(UUID roomUuid, String editorText) {
    Room room = roomRepository.findByRoomUuid(roomUuid)
        .orElseThrow(() -> {
          LOGGER.info("Room with UUID={} not found", roomUuid);
          return new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Room with UUID=%s not found", roomUuid));
        });
    room.setEditorText(editorText);
    return roomRepository.save(room);
  }
}
