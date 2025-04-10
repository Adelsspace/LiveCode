package ru.hh.blokshnote.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hh.blokshnote.entity.Room;

import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, Long> {
  Optional<Room> findByRoomUuid(UUID roomUuid);
}