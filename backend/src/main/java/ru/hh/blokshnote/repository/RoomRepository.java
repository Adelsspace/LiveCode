package ru.hh.blokshnote.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hh.blokshnote.entity.Room;

import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, Long> {
  Room findByRoomUuid(UUID roomUuid);
}