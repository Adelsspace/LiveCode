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
import ru.hh.blokshnote.dto.websocket.EditorStateDto;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.entity.User;
import ru.hh.blokshnote.repository.RoomRepository;
import ru.hh.blokshnote.repository.UserRepository;
import ru.hh.blokshnote.utility.security.RoomSecurityUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RoomService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RoomService.class);

  private static final Duration ROOM_TIME_TO_LIVE = Duration.ofHours(3);
  private static final String INITIAL_EDITOR_TEXT = "//Начните писать код";
  private static final String INITIAL_EDITOR_LANGUAGE = "javascript";
  @Value("${socketio.host-frontend}")
  private String serverHost;

  @Value("${socketio.protocol}")
  private String protocol;

  private final RoomRepository roomRepository;
  private final UserRepository userRepository;
  private final DiffService diffService;

  public RoomService(RoomRepository roomRepository, UserRepository userRepository, DiffService diffService) {
    this.roomRepository = roomRepository;
    this.userRepository = userRepository;
    this.diffService = diffService;
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
    room.setEditorText(INITIAL_EDITOR_TEXT);
    room.setEditorLanguage(INITIAL_EDITOR_LANGUAGE);
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

  @Transactional
  public User addAdminToRoom(UUID roomUuid, UUID adminToken, CreateUserRequest request) {
    Room room = getRoomByUuid(roomUuid);
    RoomSecurityUtils.verifyAdminToken(room, adminToken);

    userRepository.findByNameAndRoom(request.getUsername(), room)
        .ifPresent(user -> {
          throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this name already exists in the room");
        });
    User user = new User();
    user.setName(request.getUsername());
    user.setAdmin(true);
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
    return new WebSocketUrlDto(String.format("%s://%s%s", protocol, serverHost, WebSocketConfig.ROOM_URI_TEMPLATE));
  }

  @Transactional
  public Room updateRoomEditor(UUID roomUuid, EditorStateDto messageDto) {
    Room room = roomRepository.findByRoomUuid(roomUuid)
        .orElseThrow(() -> {
          LOGGER.info("Room with UUID={} not found", roomUuid);
          return new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Room with UUID=%s not found", roomUuid));
        });
    diffService.makeDiff(room, messageDto.getText());
    room.setEditorText(messageDto.getText());
    room.setEditorLanguage(messageDto.getLanguage());
    return roomRepository.save(room);
  }

  @Transactional
  public void updateRoomEditorLanguage(UUID roomUuid, String language) {
    Room room = roomRepository.findByRoomUuid(roomUuid)
        .orElseThrow(() -> {
          LOGGER.info("Room with UUID={} not found", roomUuid);
          return new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Room with UUID=%s not found", roomUuid));
        });
    room.setEditorLanguage(language);
    roomRepository.save(room);
  }

  @Transactional(readOnly = true)
  public User getUser(UUID roomUuid, String userName) {
    return userRepository.findByNameAndRoomRoomUuid(userName, roomUuid)
        .orElseThrow(() -> {
          LOGGER.info("User with name={} in Room with UUID={} not found", userName, roomUuid);
          return new ResponseStatusException(HttpStatus.NOT_FOUND,
              "User with name=%s in Room with UUID=%s not found".formatted(userName, roomUuid));
        });
  }
}
