package ru.hh.blokshnote.handler;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.ClientOperations;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.hh.blokshnote.config.WebSocketConfig;
import ru.hh.blokshnote.dto.websocket.ClosingRoomDto;
import ru.hh.blokshnote.dto.websocket.CursorPositionDto;
import ru.hh.blokshnote.dto.websocket.EditorStateDto;
import ru.hh.blokshnote.dto.websocket.LanguageChangeDto;
import ru.hh.blokshnote.dto.websocket.OpeningRoomDto;
import ru.hh.blokshnote.dto.websocket.TextSelectionDto;
import ru.hh.blokshnote.dto.websocket.TextUpdateDto;
import ru.hh.blokshnote.dto.websocket.UserActivityDto;
import ru.hh.blokshnote.dto.websocket.UserStateDto;
import ru.hh.blokshnote.dto.websocket.UsersUpdateDto;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.entity.User;
import ru.hh.blokshnote.service.RoomService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.hh.blokshnote.utility.WsMessageType.CLOSE_ROOM;
import static ru.hh.blokshnote.utility.WsMessageType.CURSOR_POSITION;
import static ru.hh.blokshnote.utility.WsMessageType.LANGUAGE_CHANGE;
import static ru.hh.blokshnote.utility.WsMessageType.NEW_COMMENT;
import static ru.hh.blokshnote.utility.WsMessageType.NEW_EDITOR_STATE;
import static ru.hh.blokshnote.utility.WsMessageType.NEW_EDITOR_STATE_SEND_ALL;
import static ru.hh.blokshnote.utility.WsMessageType.OPEN_ROOM;
import static ru.hh.blokshnote.utility.WsMessageType.TEXT_SELECTION;
import static ru.hh.blokshnote.utility.WsMessageType.TEXT_UPDATE;
import static ru.hh.blokshnote.utility.WsMessageType.TEXT_UPDATE_SEND_ALL;
import static ru.hh.blokshnote.utility.WsMessageType.USERS_UPDATE;
import static ru.hh.blokshnote.utility.WsMessageType.USER_ACTIVITY;
import static ru.hh.blokshnote.utility.WsPathParam.ROOM_UUID;
import static ru.hh.blokshnote.utility.WsPathParam.USER;

@Component
public class RoomSocketHandler {

  @Value("${socketio.broadcast.debug:false}")
  private boolean isBroadcastEnable;

  private static final Logger LOGGER = LoggerFactory.getLogger(RoomSocketHandler.class);
  private final String USER_STATE_KEY = "USER_STATE";
  private final RoomService roomService;
  private final SocketIOServer socketIOServer;

  public RoomSocketHandler(RoomService roomService, @Lazy SocketIOServer server) {
    this.roomService = roomService;
    this.socketIOServer = server;
  }

  public void registerListeners(SocketIONamespace namespace) {
    namespace.addConnectListener(this::connectHandler);
    namespace.addDisconnectListener(this::disconnectHandler);

    namespace.addEventListener(NEW_EDITOR_STATE.name(), EditorStateDto.class, this::editorStateEventHandler);
    namespace.addEventListener(TEXT_SELECTION.name(), TextSelectionDto.class, this::textSelectionEventHandler);
    namespace.addEventListener(CURSOR_POSITION.name(), CursorPositionDto.class, this::cursorPositionEventHandler);
    namespace.addEventListener(USER_ACTIVITY.name(), UserActivityDto.class, this::userActivityEventHandler);
    namespace.addEventListener(LANGUAGE_CHANGE.name(), LanguageChangeDto.class, this::languageChangeEventHandler);
    namespace.addEventListener(TEXT_UPDATE.name(), TextUpdateDto.class, this::textUpdateEventHandler);
    namespace.addEventListener(CLOSE_ROOM.name(), ClosingRoomDto.class, this::closeRoomEventHandler);
    namespace.addEventListener(OPEN_ROOM.name(), OpeningRoomDto.class, this::openRoomEventHandler);
    if (isBroadcastEnable) {
      namespace.addEventListener(NEW_EDITOR_STATE_SEND_ALL.name(), EditorStateDto.class, this::editorStateSendAllEventHandler);
      namespace.addEventListener(TEXT_UPDATE_SEND_ALL.name(), TextUpdateDto.class, this::textUpdateSendAllEventHandler);
    }
  }

  private void connectHandler(SocketIOClient client) {
    String roomUuidString = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    UUID roomUuid = UUID.fromString(roomUuidString);
    String userName = client.getHandshakeData().getSingleUrlParam(USER.getLabel());
    LOGGER.info("User with name={} requested connection to room with UUID={}", userName, roomUuidString);

    User user = roomService.getUser(roomUuid, userName);
    boolean isRoomClosed = roomService.isRoomClosed(roomUuid);
    if (isRoomClosed && !user.isAdmin()) {
      LOGGER.info("Room with uuid={} is closed. Only admin can connect to closed room", roomUuid);
      client.disconnect();
      return;
    }
    client.set(USER_STATE_KEY, new UserStateDto(userName, true, user.isAdmin(), user.getColor()));
    client.joinRoom(roomUuidString);

    sendEditorState(client, roomUuid);
    broadcastRoomUsers(client.getNamespace(), roomUuidString);
    LOGGER.info("User with name={} connected to room with UUID={}", userName, roomUuidString);
  }

  private void disconnectHandler(SocketIOClient client) {
    String roomUuidString = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    broadcastRoomUsers(client.getNamespace(), roomUuidString);
  }

  private void sendEditorState(SocketIOClient client, UUID roomUuid) {
    Room room = roomService.getRoomByUuid(roomUuid);

    EditorStateDto dto = new EditorStateDto();
    dto.setText(room.getEditorText());
    dto.setLanguage(room.getEditorLanguage());
    client.sendEvent(NEW_EDITOR_STATE.name(), dto);
  }

  private void broadcastRoomUsers(SocketIONamespace namespace, String roomUuid) {
    List<UserStateDto> users = namespace.getRoomOperations(roomUuid).getClients().stream()
        .map(client -> (UserStateDto) client.get(USER_STATE_KEY))
        .toList();
    LOGGER.info(
        "Users={} now in room with UUID={}",
        users.stream()
            .map(UserStateDto::getUsername)
            .collect(Collectors.toSet()),
        roomUuid
    );
    namespace.getRoomOperations(roomUuid).sendEvent(USERS_UPDATE.name(), new UsersUpdateDto(users));
  }

  private void editorStateEventHandler(SocketIOClient client, EditorStateDto data, AckRequest ackSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    LOGGER.info("Updating editor text={} and language={} in room with UUID={}",
        data.getText(), data.getLanguage(), roomUuid
    );
    Room room = roomService.updateRoomEditor(UUID.fromString(roomUuid), data);
    broadcastEditorState(client, room);
  }

  private void broadcastEditorState(SocketIOClient client, Room room) {
    String roomUuid = room.getRoomUuid().toString();
    EditorStateDto dto = new EditorStateDto();
    dto.setText(room.getEditorText());
    dto.setLanguage(room.getEditorLanguage());
    client.getNamespace().getRoomOperations(roomUuid).getClients()
        .stream()
        .filter(roomClient -> !roomClient.equals(client))
        .forEach(roomClient -> roomClient.sendEvent(NEW_EDITOR_STATE.name(), dto));
  }

  private void textSelectionEventHandler(SocketIOClient client, TextSelectionDto data, AckRequest ackSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    LOGGER.info("In room with UUID={} user={} highlighted text", roomUuid, data.getUsername());
    SocketIONamespace namespace = client.getNamespace();
    namespace.getRoomOperations(roomUuid).getClients()
        .stream()
        .filter(roomClient -> !roomClient.equals(client))
        .forEach(roomClient -> roomClient.sendEvent(TEXT_SELECTION.name(), data));
  }

  private void cursorPositionEventHandler(SocketIOClient client, CursorPositionDto data, AckRequest ackSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    LOGGER.info("In room with UUID={} user={} moved cursor", roomUuid, data.getUsername());
    SocketIONamespace namespace = client.getNamespace();
    namespace.getRoomOperations(roomUuid).getClients()
        .stream()
        .filter(roomClient -> !roomClient.equals(client))
        .forEach(roomClient -> roomClient.sendEvent(CURSOR_POSITION.name(), data));
  }

  private void userActivityEventHandler(SocketIOClient client, UserActivityDto data, AckRequest ackSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    UserStateDto userState = client.get(USER_STATE_KEY);
    if (userState == null) {
      userState = setUserState(roomUuid, client);
    }
    userState.setActive(data.isActive());
    client.set(USER_STATE_KEY, userState);
    LOGGER.info("In room with UUID={} user={} now {}", roomUuid, data.getUsername(),
        data.isActive() ? "active" : "inactive"
    );
    SocketIONamespace namespace = client.getNamespace();
    namespace.getRoomOperations(roomUuid).sendEvent(USER_ACTIVITY.name(), data);
  }

  private void languageChangeEventHandler(SocketIOClient client, LanguageChangeDto data, AckRequest ackSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    Room room = roomService.updateRoomEditorLanguage(UUID.fromString(roomUuid), data.getLanguage());
    LOGGER.info("In room with UUID={} user={} changed language to {}", roomUuid, data.getUsername(), data.getLanguage());
    SocketIONamespace namespace = client.getNamespace();
    if (!room.isModifiedByWritingCode()) {
      changeTemplateInRoom(roomUuid, namespace, data.getLanguage());
    } else {
      namespace.getRoomOperations(roomUuid).getClients()
          .stream()
          .filter(roomClient -> !roomClient.equals(client))
          .forEach(roomClient -> roomClient.sendEvent(LANGUAGE_CHANGE.name(), data));
    }
  }

  private void textUpdateEventHandler(SocketIOClient client, TextUpdateDto data, AckRequest ackSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    LOGGER.info("In room with UUID={} user={} updated text", roomUuid, data.getUsername());
    SocketIONamespace namespace = client.getNamespace();
    namespace.getRoomOperations(roomUuid).getClients()
        .stream()
        .filter(roomClient -> !roomClient.equals(client))
        .forEach(roomClient -> roomClient.sendEvent(TEXT_UPDATE.name(), data));
  }

  private void closeRoomEventHandler(SocketIOClient client, ClosingRoomDto data, AckRequest acSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    UserStateDto userState = client.get(USER_STATE_KEY);
    if (userState == null) {
      userState = setUserState(roomUuid, client);
      client.set(USER_STATE_KEY, userState);
    }
    if (!userState.isAdmin()) {
      LOGGER.info("User={} is not an admin. Only admin can close the room", data.getUsername());
      return;
    }
    roomService.changeRoomState(UUID.fromString(roomUuid), true);
    LOGGER.info("Room with UUID={} closed by user={}", roomUuid, data.getUsername());
    SocketIONamespace namespace = client.getNamespace();
    namespace.getRoomOperations(roomUuid).sendEvent(CLOSE_ROOM.name(), data);
    disconnectNonAdmins(namespace);
  }

  private void disconnectNonAdmins(SocketIONamespace namespace) {
    namespace.getAllClients().stream()
        .filter(roomClient -> {
          UserStateDto userState = roomClient.get(USER_STATE_KEY);
          return (userState == null || !userState.isAdmin());
        })
        .forEach(ClientOperations::disconnect);
  }

  private void openRoomEventHandler(SocketIOClient client, OpeningRoomDto data, AckRequest ackSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    UserStateDto userState = client.get(USER_STATE_KEY);
    if (userState == null) {
      userState = setUserState(roomUuid, client);
      client.set(USER_STATE_KEY, userState);
    }
    if (!userState.isAdmin()) {
      LOGGER.info("User={} is not an admin. Only admin can open the room", data.getUsername());
      return;
    }
    roomService.changeRoomState(UUID.fromString(roomUuid), false);
    LOGGER.info("Room with UUID={} opened by user={}", roomUuid, data.getUsername());
    SocketIONamespace namespace = client.getNamespace();
    namespace.getRoomOperations(roomUuid).sendEvent(OPEN_ROOM.name(), data);
  }

  public void broadcastNewCommentToAdmins(UUID uuidOfRoom) {
    SocketIONamespace namespace = socketIOServer.getNamespace(WebSocketConfig.ROOM_URI_TEMPLATE);
    if (namespace == null) {
      LOGGER.error("Namespace not initialized in RoomSocketHandler. Cannot broadcast NEW_COMMENT.");
      return;
    }
    String roomUuid = String.valueOf(uuidOfRoom);
    LOGGER.info("Broadcasting NEW_COMMENT notification to admins in room {}", roomUuid);
    namespace.getRoomOperations(roomUuid).getClients()
        .stream()
        .filter(client -> {
          UserStateDto userState = client.get(USER_STATE_KEY);
          return (userState != null && userState.isAdmin());
        })
        .forEach(client -> client.sendEvent(NEW_COMMENT.name()));
  }

  private UserStateDto setUserState(String roomUuid, SocketIOClient client) {
    String userName = client.getHandshakeData().getSingleUrlParam(USER.getLabel());
    User user = roomService.getUser(UUID.fromString(roomUuid), userName);
    UserStateDto userState = new UserStateDto();
    userState.setUsername(userName);
    userState.setAdmin(user.isAdmin());
    userState.setActive(true);
    return userState;
  }

  private void editorStateSendAllEventHandler(SocketIOClient client, EditorStateDto data, AckRequest ackSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    LOGGER.info("Updating editor text={} and language={} in room with UUID={}",
        data.getText(), data.getLanguage(), roomUuid
    );
    Room room = roomService.updateRoomEditor(UUID.fromString(roomUuid), data);
    EditorStateDto dto = new EditorStateDto();
    dto.setText(room.getEditorText());
    dto.setLanguage(room.getEditorLanguage());
    client.getNamespace().getRoomOperations(roomUuid).sendEvent(NEW_EDITOR_STATE.name(), dto);
  }

  private void textUpdateSendAllEventHandler(SocketIOClient client, TextUpdateDto data, AckRequest ackSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    LOGGER.info("In room with UUID={} user={} updated text", roomUuid, data.getUsername());
    SocketIONamespace namespace = client.getNamespace();
    namespace.getRoomOperations(roomUuid).sendEvent(TEXT_UPDATE_SEND_ALL.name(), data);
  }

  private void changeTemplateInRoom(String roomUuid, SocketIONamespace namespace, String alias) {
    Room roomTemplate = roomService.changeRoomTemplate(UUID.fromString(roomUuid), alias);
    EditorStateDto dto = new EditorStateDto();
    dto.setText(roomTemplate.getEditorText());
    dto.setLanguage(roomTemplate.getEditorLanguage());
    namespace.getRoomOperations(roomUuid).sendEvent(NEW_EDITOR_STATE.name(), dto);
  }
}
