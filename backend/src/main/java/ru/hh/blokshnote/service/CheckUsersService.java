package ru.hh.blokshnote.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hh.blokshnote.config.WebSocketConfig;
import ru.hh.blokshnote.dto.websocket.UserState;
import ru.hh.blokshnote.dto.websocket.UsersUpdateDto;
import ru.hh.blokshnote.entity.User;
import ru.hh.blokshnote.handler.RoomSocketHandler;
import ru.hh.blokshnote.mapper.UserStateMapper;
import ru.hh.blokshnote.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.hh.blokshnote.utility.WsMessageType.USERS_UPDATE;
import static ru.hh.blokshnote.utility.WsPathParam.ROOM_UUID;

@Service
public class CheckUsersService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CheckUsersService.class);

  private final SocketIOServer socketIOServer;
  private final UserRepository userRepository;

  public CheckUsersService(SocketIOServer socketIOServer, UserRepository userRepository) {
    this.socketIOServer = socketIOServer;
    this.userRepository = userRepository;
  }

  @Transactional
  @Scheduled(cron = "*/2 * * * * *")
  public void checkUsersInRooms() {
    List<SocketIOClient> clients = socketIOServer.getNamespace(WebSocketConfig.ROOM_URI_TEMPLATE).getAllClients().stream()
        .toList();
    if (clients.isEmpty()) {
      LOGGER.info("checkUsersInRooms(): clients not found");
      return;
    }
    List<Long> userIds = clients.stream()
        .filter(client -> client.get(RoomSocketHandler.USER_STATE_KEY) != null)
        .map(client -> ((UserState) client.get(RoomSocketHandler.USER_STATE_KEY)).getId())
        .toList();
    LOGGER.info("Founded {} connected clients", userIds.size());
    List<User> users = userRepository.findAllById(userIds);
    Instant lastPingTime = Instant.now();
    users.forEach(user -> user.setLastPingTime(lastPingTime));

    userRepository.saveAll(users);

    Map<String, List<SocketIOClient>> roomClientMap = clients.stream()
        .filter(client -> client.get(RoomSocketHandler.USER_STATE_KEY) != null)
        .collect(Collectors.groupingBy(
            client -> client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel()), Collectors.toList())
        );

    long secondsBeforeDisconnect = 5;
    Map<String, UsersUpdateDto> connectedUsers = userRepository.findAllByLastPingTimeAfter(
            Instant.now().minusSeconds(secondsBeforeDisconnect)
        ).stream()
        .collect(Collectors.groupingBy(user -> user.getRoom().getRoomUuid().toString(),
            Collectors.mapping(UserStateMapper::toDto, Collectors.toList())))
        .entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> new UsersUpdateDto(entry.getValue())));

    roomClientMap.forEach((roomUuid, clientsForBroadcast) -> clientsForBroadcast.forEach(
        client -> client.sendEvent(USERS_UPDATE.name(), connectedUsers.get(roomUuid))));
  }
}
