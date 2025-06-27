package ru.hh.blokshnote.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.entity.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByNameAndRoom(String name, Room room);

  Optional<User> findByNameAndRoomRoomUuid(String name, UUID roomUuid);

  List<User> findAllByRoom(Room room);

  void deleteByRoomIdIn(List<Long> roomIds);

  List<User> findAllByLastPingTimeAfter(Instant onlineTime);
}
