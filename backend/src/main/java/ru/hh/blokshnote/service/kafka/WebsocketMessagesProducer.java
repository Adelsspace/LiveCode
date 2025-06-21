package ru.hh.blokshnote.service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import static ru.hh.blokshnote.config.KafkaConfig.WEBSOCKET_TOPIC;
import ru.hh.blokshnote.dto.kafka.KafkaRoomEvent;
import ru.hh.blokshnote.utility.WsMessageType;

@Service
public class WebsocketMessagesProducer {
  private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketMessagesProducer.class);

  private final KafkaTemplate<String, KafkaRoomEvent> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public WebsocketMessagesProducer(KafkaTemplate<String, KafkaRoomEvent> kafkaTemplate, ObjectMapper objectMapper) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
  }

  public void publishRoomEvent(String roomUuid, WsMessageType eventType, Object data, String originatingSessionId) {
    try {
      String payload = objectMapper.writeValueAsString(data);
      KafkaRoomEvent event = new KafkaRoomEvent(roomUuid, eventType, payload, originatingSessionId);

      kafkaTemplate.send(WEBSOCKET_TOPIC, roomUuid, event);
      LOGGER.info("Published event {} to Kafka for room {}", eventType, roomUuid);
    } catch (Exception e) {
      LOGGER.error("Failed to publish event to Kafka for room {}", roomUuid, e);
    }
  }
}
