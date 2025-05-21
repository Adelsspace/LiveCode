package ru.hh.blokshnote.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByNameAndRoom(String name, Room room);

  Optional<User> findByNameAndRoomRoomUuid(String name, UUID roomUuid);

}
