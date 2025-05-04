package ru.hh.blokshnote.handler;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.hh.blokshnote.dto.room.request.RoomStateMessageDto;
import ru.hh.blokshnote.dto.room.response.RoomStateDto;
import ru.hh.blokshnote.dto.websocket.CursorPositionDto;
import ru.hh.blokshnote.dto.websocket.LanguageChangeDto;
import ru.hh.blokshnote.dto.websocket.TextSelectionDto;
import ru.hh.blokshnote.dto.websocket.TextUpdateDto;
import ru.hh.blokshnote.dto.websocket.UserActivityDto;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.service.RoomService;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.hh.blokshnote.utility.WsMessageType.CURSOR_POSITION;
import static ru.hh.blokshnote.utility.WsMessageType.LANGUAGE_CHANGE;
import static ru.hh.blokshnote.utility.WsMessageType.NEW_ROOM_STATE;
import static ru.hh.blokshnote.utility.WsMessageType.ROOM_STATE_UPDATE;
import static ru.hh.blokshnote.utility.WsMessageType.TEXT_SELECTION;
import static ru.hh.blokshnote.utility.WsMessageType.TEXT_UPDATE;
import static ru.hh.blokshnote.utility.WsMessageType.USER_ACTIVITY;
import static ru.hh.blokshnote.utility.WsPathParam.ROOM_UUID;
import static ru.hh.blokshnote.utility.WsPathParam.USER;

@Component
public class RoomSocketHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(RoomSocketHandler.class);
  private final RoomService roomService;
  private final ObjectMapper objectMapper;

  public RoomSocketHandler(RoomService roomService) {
    this.roomService = roomService;
    this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
  }

  public void registerListeners(SocketIONamespace namespace) {
    namespace.addConnectListener(client -> {
      String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
      client.joinRoom(roomUuid);
      Room room = roomService.getRoomByUuid(UUID.fromString(roomUuid));
      broadcastRoomState(namespace, room);
    });

    namespace.addDisconnectListener(client -> {
      String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
      Room room = roomService.getRoomByUuid(UUID.fromString(roomUuid));
      broadcastRoomState(namespace, room);
    });

    namespace.addEventListener(NEW_ROOM_STATE.name(), RoomStateMessageDto.class, (client, data, ackSender) -> {
      String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
      Room room = roomService.updateEditorText(UUID.fromString(roomUuid), data);
      broadcastRoomState(namespace, room);
    });

    namespace.addEventListener(TEXT_SELECTION.name(), TextSelectionDto.class, this::textSelectionEventHandler);
    namespace.addEventListener(CURSOR_POSITION.name(), CursorPositionDto.class, this::cursorPositionEventHandler);
    namespace.addEventListener(USER_ACTIVITY.name(), UserActivityDto.class, this::userActivityEventHandler);
    namespace.addEventListener(LANGUAGE_CHANGE.name(), LanguageChangeDto.class, this::languageChangeEventHandler);
    namespace.addEventListener(TEXT_UPDATE.name(), TextUpdateDto.class, this::textUpdateEventHandler);
  }

  private void broadcastRoomState(SocketIONamespace namespace, Room room) {
    String roomUuid = room.getRoomUuid().toString();
    Set<String> users = namespace.getRoomOperations(roomUuid).getClients().stream()
        .map(client -> client.getHandshakeData().getSingleUrlParam(USER.getLabel()))
        .collect(Collectors.toSet());

    RoomStateDto dto = new RoomStateDto();
    dto.setEditorText(room.getEditorText());
    dto.setUsers(users);

    namespace.getRoomOperations(roomUuid).sendEvent(ROOM_STATE_UPDATE.name(), dto);
  }

  private void textSelectionEventHandler(SocketIOClient client, TextSelectionDto data, AckRequest ackSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    SocketIONamespace namespace = client.getNamespace();
    namespace.getRoomOperations(roomUuid).sendEvent(TEXT_SELECTION.name(), data);
  }

  private void cursorPositionEventHandler(SocketIOClient client, CursorPositionDto data, AckRequest ackSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    SocketIONamespace namespace = client.getNamespace();
    namespace.getRoomOperations(roomUuid).sendEvent(CURSOR_POSITION.name(), data);
  }

  private void userActivityEventHandler(SocketIOClient client, UserActivityDto data, AckRequest ackSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    SocketIONamespace namespace = client.getNamespace();
    namespace.getRoomOperations(roomUuid).sendEvent(USER_ACTIVITY.name(), data);
  }

  private void languageChangeEventHandler(SocketIOClient client, LanguageChangeDto data, AckRequest ackSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    SocketIONamespace namespace = client.getNamespace();
    namespace.getRoomOperations(roomUuid).sendEvent(LANGUAGE_CHANGE.name(), data);
  }

  private void textUpdateEventHandler(SocketIOClient client, TextUpdateDto data, AckRequest ackSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    SocketIONamespace namespace = client.getNamespace();
    namespace.getRoomOperations(roomUuid).sendEvent(TEXT_UPDATE.name(), data);
  }
}