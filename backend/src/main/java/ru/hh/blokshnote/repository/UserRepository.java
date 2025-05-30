package ru.hh.blokshnote.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByNameAndRoom(String name, Room room);

  Optional<User> findByNameAndRoomRoomUuid(String name, UUID roomUuid);

  List<User> findAllByRoom(Room room);

}
