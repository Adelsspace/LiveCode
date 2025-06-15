package ru.hh.blokshnote.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hh.blokshnote.entity.Room;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, Long> {
  Optional<Room> findByRoomUuid(UUID roomUuid);

  List<Room> findAllByUpdatedAtBefore(Instant date);
}