package ru.hh.blokshnote.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.mapper.RoomMapper;
import ru.hh.blokshnote.service.RoomService;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static ru.hh.blokshnote.utility.WsMessageType.NEW_ROOM_STATE;
import static ru.hh.blokshnote.utility.WsPathParam.MESSAGE_TYPE;
import static ru.hh.blokshnote.utility.WsPathParam.ROOM_UUID;
import static ru.hh.blokshnote.utility.WsPathParam.USER;

@Component
public class SimpleTextWebSocketHandler extends TextWebSocketHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleTextWebSocketHandler.class);
  private final RoomService roomService;
  private final RoomMapper roomMapper;
  private final Map<String, Set<WebSocketSession>> roomsSessions;
  private final ObjectMapper objectMapper;

  public SimpleTextWebSocketHandler(RoomService roomService, RoomMapper roomMapper) {
    this.roomService = roomService;
    this.roomMapper = roomMapper;
    this.roomsSessions = new ConcurrentHashMap<>();
    this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    Map<String, String> params = getUriParams(session.getUri());
    String roomUuid = params.get(ROOM_UUID.getLabel());
    String user = params.get(USER.getLabel());
    Room room = roomService.getRoomByUuid(UUID.fromString(roomUuid));
    roomsSessions.computeIfAbsent(roomUuid, t -> new HashSet<>()).add(session);
    LOGGER.info("User {} connected to room {}", user, roomUuid);

    broadcastToRoom(room);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    Map<String, String> params = getUriParams(session.getUri());
    String roomUuid = params.get(ROOM_UUID.getLabel());
    String messageType = params.get(MESSAGE_TYPE.getLabel());
    if (NEW_ROOM_STATE.name().equals(messageType)) {
      Room room = roomService.updateEditorText(UUID.fromString(roomUuid), message.getPayload());
      broadcastToRoom(room);
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    Map<String, String> params = getUriParams(session.getUri());
    String roomUuid = params.get(ROOM_UUID.getLabel());
    Set<WebSocketSession> sessions = roomsSessions.get(roomUuid);
    if (sessions != null) {
      sessions.remove(session);
    }
    LOGGER.info("User disconnected from room: {}", roomUuid);
    Room room = roomService.getRoomByUuid(UUID.fromString(roomUuid));

    broadcastToRoom(room);
  }

  private Map<String, String> getUriParams(URI uri) {
    if (uri == null) return Collections.emptyMap();
    String query = uri.getQuery();
    if (query == null) return Collections.emptyMap();

    return Arrays.stream(query.split("&"))
        .map(p -> p.split("=", 2))
        .filter(p -> p.length == 2)
        .collect(Collectors.toMap(p -> p[0], p -> p[1]));
  }

  private void broadcastToRoom(Room room) {
    String roomUuid = room.getRoomUuid().toString();
    Set<String> users = roomsSessions.get(roomUuid).stream()
        .map(roomSession -> getUriParams(roomSession.getUri()).get(USER.getLabel()))
        .collect(Collectors.toSet());
    roomsSessions.getOrDefault(roomUuid, Collections.emptySet())
        .forEach(session -> {
          try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(roomMapper.toRoomStateDto(room, users))));
          } catch (JsonProcessingException e) {
            LOGGER.warn("Error while mapping object to json: {}", e.getMessage());
          } catch (IOException e) {
            LOGGER.info("Error while sending message: {}", e.getMessage());
          }
        });
  }
}
