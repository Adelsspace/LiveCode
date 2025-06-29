package ru.hh.blokshnote.dto.kafka;

import ru.hh.blokshnote.utility.WsMessageType;

public record KafkaRoomEvent(String roomUuid, WsMessageType eventType, String payload, String senderId) {
}
