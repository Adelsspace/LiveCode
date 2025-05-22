package ru.hh.blokshnote.handler;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.hh.blokshnote.config.WebSocketConfig;
import ru.hh.blokshnote.dto.websocket.CursorPositionDto;
import ru.hh.blokshnote.dto.websocket.EditorStateDto;
import ru.hh.blokshnote.dto.websocket.LanguageChangeDto;
import ru.hh.blokshnote.dto.websocket.TextSelectionDto;
import ru.hh.blokshnote.dto.websocket.TextUpdateDto;
import ru.hh.blokshnote.dto.websocket.UserActivityDto;
import ru.hh.blokshnote.dto.websocket.UserStateDto;
import ru.hh.blokshnote.dto.websocket.UsersUpdateDto;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.entity.User;
import ru.hh.blokshnote.service.RoomService;
import static ru.hh.blokshnote.utility.WsMessageType.CURSOR_POSITION;
import static ru.hh.blokshnote.utility.WsMessageType.LANGUAGE_CHANGE;
import static ru.hh.blokshnote.utility.WsMessageType.NEW_COMMENT;
import static ru.hh.blokshnote.utility.WsMessageType.NEW_EDITOR_STATE;
import static ru.hh.blokshnote.utility.WsMessageType.TEXT_SELECTION;
import static ru.hh.blokshnote.utility.WsMessageType.TEXT_UPDATE;
import static ru.hh.blokshnote.utility.WsMessageType.USERS_UPDATE;
import static ru.hh.blokshnote.utility.WsMessageType.USER_ACTIVITY;
import static ru.hh.blokshnote.utility.WsPathParam.ROOM_UUID;
import static ru.hh.blokshnote.utility.WsPathParam.USER;
import ru.hh.blokshnote.utility.colors.UserColorUtil;

@Component
public class RoomSocketHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(RoomSocketHandler.class);
  private final String USER_STATE_KEY = "USER_STATE";
  private final RoomService roomService;
  private final SocketIONamespace socketIONamespace;

  public RoomSocketHandler(RoomService roomService, @Lazy SocketIONamespace namespace) {
    this.roomService = roomService;
    this.socketIONamespace = namespace;
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
  }

  private void connectHandler(SocketIOClient client) {
    String roomUuidString = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    UUID roomUuid = UUID.fromString(roomUuidString);
    String userName = client.getHandshakeData().getSingleUrlParam(USER.getLabel());
    LOGGER.info("User with name={} requested connection to room with UUID={}", userName, roomUuidString);
    User user = roomService.getUser(roomUuid, userName);
    client.set(USER_STATE_KEY, new UserStateDto(userName, true, user.isAdmin(), UserColorUtil.generateUserColor(userName)));
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
        data.getText(), data.getLanguage(), roomUuid);
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
      String userName = client.getHandshakeData().getSingleUrlParam(USER.getLabel());
      User user = roomService.getUser(UUID.fromString(roomUuid), userName);
      userState = new UserStateDto();
      userState.setUsername(userName);
      userState.setAdmin(user.isAdmin());
    }
    userState.setActive(data.isActive());
    client.set(USER_STATE_KEY, userState);
    LOGGER.info("In room with UUID={} user={} now {}", roomUuid, data.getUsername(),
        data.isActive() ? "active" : "inactive"
    );
    SocketIONamespace namespace = client.getNamespace();
    namespace.getRoomOperations(roomUuid).getClients()
        .stream()
        .filter(roomClient -> !roomClient.equals(client))
        .forEach(roomClient -> roomClient.sendEvent(USER_ACTIVITY.name(), data));
  }

  private void languageChangeEventHandler(SocketIOClient client, LanguageChangeDto data, AckRequest ackSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    roomService.updateRoomEditorLanguage(UUID.fromString(roomUuid), data.getLanguage());
    LOGGER.info("In room with UUID={} user={} changed language to {}", roomUuid, data.getUsername(), data.getLanguage());
    SocketIONamespace namespace = client.getNamespace();
    namespace.getRoomOperations(roomUuid).getClients()
        .stream()
        .filter(roomClient -> !roomClient.equals(client))
        .forEach(roomClient -> roomClient.sendEvent(LANGUAGE_CHANGE.name(), data));
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

  public void broadcastNewCommentToAdmins(UUID uuidOfRoom) {
    if (this.socketIONamespace == null) {
      LOGGER.error("Namespace not initialized in RoomSocketHandler. Cannot broadcast NEW_COMMENT.");
      return;
    }
    String roomUuid = String.valueOf(uuidOfRoom);
    LOGGER.info("Broadcasting NEW_COMMENT notification to admins in room {}", roomUuid);
    socketIONamespace.getRoomOperations(roomUuid).getClients()
        .stream()
        .filter(client -> {
          UserStateDto userState = client.get(USER_STATE_KEY);
          return (userState != null && userState.isAdmin());
        })
        .forEach(client -> client.sendEvent(NEW_COMMENT.name()));
  }
}
