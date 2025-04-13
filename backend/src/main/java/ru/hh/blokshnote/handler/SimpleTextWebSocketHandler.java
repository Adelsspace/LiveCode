package ru.hh.blokshnote.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.hh.blokshnote.repository.RoomRepository;

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

@Component
public class SimpleTextWebSocketHandler extends TextWebSocketHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleTextWebSocketHandler.class);

  @Autowired
  private RoomRepository roomRepository;

  private final Map<String, Set<WebSocketSession>> roomsSessions = new ConcurrentHashMap<>();


  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    Map<String, String> params = getUriParams(session.getUri());
    String roomId = params.get("roomId");
    roomRepository.findByRoomUuid(UUID.fromString(roomId))
        .orElseThrow(() -> {
          LOGGER.info("Room with UUID={} not found", roomId);
          return new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Room with UUID=%s not found", roomId));
        });

    roomsSessions.computeIfAbsent(roomId, t -> new HashSet<>()).add(session);
    LOGGER.info("User connected to room: {}", roomId);
    session.sendMessage(new TextMessage("Room state on connect"));
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    Map<String, String> params = getUriParams(session.getUri());
    String roomId = params.get("roomId");
    Set<WebSocketSession> sessions = roomsSessions.get(roomId);
    sessions.forEach(clientSession -> {
      try {
        clientSession.sendMessage(new TextMessage("Room state message"));
      } catch (IOException e) {
        LOGGER.info("Error while sending message: {}", e.getMessage());
      }
    });
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    Map<String, String> params = getUriParams(session.getUri());
    String roomId = params.get("roomId");
    roomsSessions.get(roomId).remove(session);
    LOGGER.info("User disconnected from room: {}", roomId);
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
}
