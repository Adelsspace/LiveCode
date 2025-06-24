package ru.hh.blokshnote.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.repository.CommentRepository;
import ru.hh.blokshnote.repository.DiffRepository;
import ru.hh.blokshnote.repository.RoomRepository;
import ru.hh.blokshnote.repository.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class DataCleanupService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataCleanupService.class);

  @Value("${blokshnote.rooms.lifetime:14}")
  private int roomsLifetime;

  @Value("${blokshnote.rooms.cleanup:true}")
  private boolean isCleanupEnable;

  private final RoomRepository roomRepository;
  private final CommentRepository commentRepository;
  private final DiffRepository diffRepository;
  private final UserRepository userRepository;

  public DataCleanupService(
      RoomRepository roomRepository,
      CommentRepository commentRepository,
      DiffRepository diffRepository,
      UserRepository userRepository
  ) {
    this.roomRepository = roomRepository;
    this.commentRepository = commentRepository;
    this.diffRepository = diffRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  @Scheduled(cron = "0 0 3 * * *")
  public void deleteOldRooms() {
    if (!isCleanupEnable) {
      LOGGER.info("Scheduled cleanup disabled");
      return;
    }
    LOGGER.info("Scheduled cleanup started");
    Instant dateLimit = Instant.now().minus(roomsLifetime, ChronoUnit.DAYS);
    var roomIds = roomRepository.findAllByUpdatedAtBefore(dateLimit).stream()
        .map(Room::getId)
        .toList();
    if (roomIds.isEmpty()) {
      LOGGER.info("Not found room to delete");
      return;
    }

    LOGGER.info("Rooms found to delete: {}", roomIds.size());
    commentRepository.deleteByRoomIdIn(roomIds);
    diffRepository.deleteByRoomIdIn(roomIds);
    userRepository.deleteByRoomIdIn(roomIds);
    roomRepository.deleteAllById(roomIds);
    LOGGER.info("Scheduled cleanup successfully finished");
  }
}
