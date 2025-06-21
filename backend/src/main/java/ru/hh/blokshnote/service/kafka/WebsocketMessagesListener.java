package ru.hh.blokshnote.service.kafka;

import com.corundumstudio.socketio.ClientOperations;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Service;
import static ru.hh.blokshnote.config.KafkaConfig.WEBSOCKET_TOPIC;
import ru.hh.blokshnote.config.WebSocketConfig;
import ru.hh.blokshnote.dto.kafka.KafkaRoomEvent;
import ru.hh.blokshnote.dto.websocket.UserStateDto;
import static ru.hh.blokshnote.handler.RoomSocketHandler.USER_STATE_KEY;
import ru.hh.blokshnote.utility.WsMessageType;
import static ru.hh.blokshnote.utility.WsMessageType.NEW_COMMENT;

@Service
public class WebsocketMessagesListener {
  private static final Logger log = LoggerFactory.getLogger(WebsocketMessagesListener.class);

  private final SocketIOServer socketIOServer;
  private final ObjectMapper objectMapper;

  public WebsocketMessagesListener(@Lazy SocketIOServer socketIOServer, ObjectMapper objectMapper) {
    this.socketIOServer = socketIOServer;
    this.objectMapper = objectMapper;
  }

  @KafkaListener(topicPartitions = @TopicPartition(topic = WEBSOCKET_TOPIC,
      partitions = "#{@finder.partitions(T(ru.hh.blokshnote.config.KafkaConfig).WEBSOCKET_TOPIC)}"))
  public void consumeRoomEvent(KafkaRoomEvent event) {
    try {
      log.info("Consumed event {} from Kafka for room {}", event.eventType(), event.roomUuid());
      SocketIONamespace namespace = socketIOServer.getNamespace(WebSocketConfig.ROOM_URI_TEMPLATE);
      if (namespace == null) {
        log.error("Namespace not found. Cannot broadcast event from Kafka.");
        return;
      }

      Class<?> dtoClass = event.eventType().getDtoClass();
      Object payloadDto = objectMapper.readValue(event.payload(), dtoClass);

      if (event.eventType().equals(WsMessageType.NEW_COMMENT)) {
        sendCommentNotificationToAdmins(namespace, event.roomUuid());
      } else if (event.eventType().equals(WsMessageType.CLOSE_ROOM)) {
        namespace.getRoomOperations(event.roomUuid()).sendEvent(event.eventType().name(), payloadDto);
        disconnectNonAdmins(namespace, event.roomUuid());
      } else {
        namespace.getRoomOperations(event.roomUuid()).getClients().stream()
            .filter(client -> !client.getSessionId().toString().equals(event.senderId()))
            .forEach(client -> client.sendEvent(event.eventType().name(), payloadDto));
      }
    } catch (Exception e) {
      log.error("Error processing event from Kafka: {}", event, e);
    }
  }

  private void disconnectNonAdmins(SocketIONamespace namespace, String roomUuid) {
    namespace.getRoomOperations(roomUuid).getClients().stream()
        .filter(roomClient -> {
          UserStateDto userState = roomClient.get(USER_STATE_KEY);
          return (userState == null || !userState.isAdmin());
        })
        .forEach(ClientOperations::disconnect);
  }

  private void sendCommentNotificationToAdmins(SocketIONamespace namespace, String roomUuid) {
    namespace.getRoomOperations(roomUuid).getClients()
        .stream()
        .filter(client -> {
          UserStateDto userState = client.get(USER_STATE_KEY);
          return (userState != null && userState.isAdmin());
        })
        .forEach(client -> client.sendEvent(NEW_COMMENT.name()));
  }
}
