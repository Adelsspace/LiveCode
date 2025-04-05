package ru.hh.blokshnote.service;

import org.springframework.stereotype.Service;
import ru.hh.blokshnote.dto.requests.CreateRoomRequest;
import ru.hh.blokshnote.dto.requests.CreateUserRequest;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.entity.User;
import ru.hh.blokshnote.repository.RoomRepository;
import ru.hh.blokshnote.repository.UserRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RoomService {

  private final RoomRepository roomRepository;
  private final UserRepository userRepository;

  public RoomService(RoomRepository roomRepository, UserRepository userRepository) {
    this.roomRepository = roomRepository;
    this.userRepository = userRepository;
  }

  public Room createRoomWithAdmin(CreateRoomRequest request) {
    UUID roomUuid = UUID.randomUUID();
    String adminToken = UUID.randomUUID().toString();
    Instant now = Instant.now();
    Instant expireAt = now.plusSeconds(3600 * 3);

    Room room = new Room();
    room.setRoomUuid(roomUuid);
    room.setAdminToken(adminToken);
    room.setCreatedAt(now);
    room.setExpiredAt(expireAt);
    room = roomRepository.save(room);

    User adminUser = new User();
    adminUser.setName(request.getUsername());
    adminUser.setAdmin(true);
    adminUser.setRoom(room);
    userRepository.save(adminUser);

    return room;
  }

  public User addUserToRoom(UUID roomUuid, CreateUserRequest request) {
    Room room = roomRepository.findByRoomUuid(roomUuid);
    if (room == null) {
      throw new IllegalArgumentException("Room not found with UUID: " + roomUuid);
    }

    Optional<User> existingUser = userRepository.findByNameAndRoom(request.getUsername(), room);
    if (existingUser.isPresent()) {
      throw new IllegalStateException("User with name " + request.getUsername() + " already exists in this room.");
    }

    User user = new User();
    user.setName(request.getUsername());
    user.setAdmin(false);
    user.setRoom(room);

    return userRepository.save(user);
  }

  public Room getRoomByUuid(UUID roomUuid) {
    return roomRepository.findByRoomUuid(roomUuid);
  }
}


