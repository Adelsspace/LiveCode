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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.hh.blokshnote.config.WebSocketConfig;
import ru.hh.blokshnote.dto.review.request.LlmStatusDto;
import ru.hh.blokshnote.dto.websocket.ChangeDto;
import ru.hh.blokshnote.dto.websocket.ClosingRoomDto;
import ru.hh.blokshnote.dto.websocket.CursorPositionDto;
import ru.hh.blokshnote.dto.websocket.EditorStateDto;
import ru.hh.blokshnote.dto.websocket.LanguageChangeDto;
import ru.hh.blokshnote.dto.websocket.NewCommentDto;
import ru.hh.blokshnote.dto.websocket.OpeningRoomDto;
import ru.hh.blokshnote.dto.websocket.RangeDto;
import ru.hh.blokshnote.dto.websocket.TextSelectionDto;
import ru.hh.blokshnote.dto.websocket.TextUpdateDto;
import ru.hh.blokshnote.dto.websocket.UserActivityDto;
import ru.hh.blokshnote.dto.websocket.UserState;
import ru.hh.blokshnote.dto.websocket.UsersUpdateDto;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.entity.User;
import ru.hh.blokshnote.mapper.UserStateMapper;
import ru.hh.blokshnote.service.RoomService;
import ru.hh.blokshnote.service.kafka.WebsocketMessagesProducer;
import static ru.hh.blokshnote.utility.WsMessageType.CLOSE_ROOM;
import static ru.hh.blokshnote.utility.WsMessageType.CURSOR_POSITION;
import static ru.hh.blokshnote.utility.WsMessageType.LANGUAGE_CHANGE;
import static ru.hh.blokshnote.utility.WsMessageType.LLM_STATUS;
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

  public static final String USER_STATE_KEY = "USER_STATE";

  @Value("${socketio.broadcast.debug:false}")
  private boolean isBroadcastEnable;

  private static final Logger LOGGER = LoggerFactory.getLogger(RoomSocketHandler.class);
  private final RoomService roomService;
  private final SocketIOServer socketIOServer;
  private final WebsocketMessagesProducer messagesProducer;

  private final static int START_POS_FOR_TEMPLATE = 1;
  private final static int END_POS_FOR_TEMPLATE = 100;

  public RoomSocketHandler(RoomService roomService, @Lazy SocketIOServer server, WebsocketMessagesProducer messagesProducer) {
    this.roomService = roomService;
    this.socketIOServer = server;
    this.messagesProducer = messagesProducer;
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
    client.set(USER_STATE_KEY, new UserState(user.getId(), userName, user.isAdmin(), user.getColor()));
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
    List<UserState> users = namespace.getRoomOperations(roomUuid).getClients().stream()
        .map(client -> (UserState) client.get(USER_STATE_KEY))
        .toList();
    LOGGER.info(
        "Users={} now in room with UUID={}",
        users.stream()
            .map(UserState::getUsername)
            .collect(Collectors.toSet()),
        roomUuid
    );
    namespace.getRoomOperations(roomUuid).sendEvent(USERS_UPDATE.name(), new UsersUpdateDto(
        users.stream()
            .map(UserStateMapper::toDto)
            .collect(Collectors.toList()))
    );
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
    messagesProducer.publishRoomEvent(roomUuid, NEW_EDITOR_STATE, dto, client.getSessionId().toString());
  }

  private void textSelectionEventHandler(SocketIOClient client, TextSelectionDto data, AckRequest ackSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    LOGGER.info("In room with UUID={} user={} highlighted text", roomUuid, data.getUsername());
    messagesProducer.publishRoomEvent(roomUuid, TEXT_SELECTION, data, client.getSessionId().toString());
  }

  private void cursorPositionEventHandler(SocketIOClient client, CursorPositionDto data, AckRequest ackSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    LOGGER.info("In room with UUID={} user={} moved cursor", roomUuid, data.getUsername());
    messagesProducer.publishRoomEvent(roomUuid, CURSOR_POSITION, data, client.getSessionId().toString());
  }

  private void userActivityEventHandler(SocketIOClient client, UserActivityDto data, AckRequest ackSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    LOGGER.info("In room with UUID={} user={} now {}", roomUuid, data.getUsername(),
        data.isActive() ? "active" : "inactive"
    );
    messagesProducer.publishRoomEvent(roomUuid, USER_ACTIVITY, data, null);
  }

  private void languageChangeEventHandler(SocketIOClient client, LanguageChangeDto data, AckRequest ackSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    UUID uuidOfRoom = UUID.fromString(roomUuid);
    Room room = roomService.updateRoomEditorLanguage(uuidOfRoom, data.getLanguage());
    LOGGER.info("In room with UUID={} user={} changed language to {}", roomUuid, data.getUsername(), data.getLanguage());
    SocketIONamespace namespace = client.getNamespace();
    if (!room.isModifiedByWritingCode()) {
      changeTemplateInRoom(room, namespace, data.getUsername());
    }
    messagesProducer.publishRoomEvent(roomUuid, LANGUAGE_CHANGE, data, client.getSessionId().toString());
  }

  private void textUpdateEventHandler(SocketIOClient client, TextUpdateDto data, AckRequest ackSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    LOGGER.info("In room with UUID={} user={} updated text", roomUuid, data.getUsername());
    messagesProducer.publishRoomEvent(roomUuid, TEXT_UPDATE, data, client.getSessionId().toString());
  }

  private void closeRoomEventHandler(SocketIOClient client, ClosingRoomDto data, AckRequest acSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    UserState userState = client.get(USER_STATE_KEY);
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
    messagesProducer.publishRoomEvent(roomUuid, CLOSE_ROOM, data, null);
  }

  private void openRoomEventHandler(SocketIOClient client, OpeningRoomDto data, AckRequest ackSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    UserState userState = client.get(USER_STATE_KEY);
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
    messagesProducer.publishRoomEvent(roomUuid, OPEN_ROOM, data, null);
  }

  public void broadcastNewCommentToAdmins(UUID uuidOfRoom) {
    SocketIONamespace namespace = socketIOServer.getNamespace(WebSocketConfig.ROOM_URI_TEMPLATE);
    if (namespace == null) {
      LOGGER.error("Namespace not initialized in RoomSocketHandler. Cannot broadcast NEW_COMMENT.");
      return;
    }
    String roomUuid = String.valueOf(uuidOfRoom);
    LOGGER.info("Broadcasting NEW_COMMENT notification to admins in room {}", roomUuid);
    messagesProducer.publishRoomEvent(roomUuid, NEW_COMMENT, new NewCommentDto(), null);
  }


  public void broadcastLLMStatusToAdmins(UUID uuidOfRoom, boolean status) {
    SocketIONamespace namespace = socketIOServer.getNamespace(WebSocketConfig.ROOM_URI_TEMPLATE);
    if (namespace == null) {
      LOGGER.error("Namespace not initialized in RoomSocketHandler. Cannot broadcast LLM_STATUS.");
      return;
    }
    String roomUuid = String.valueOf(uuidOfRoom);
    LOGGER.info("Broadcasting LLM_STATUS={} notification to admins in room {}", status, roomUuid);
    namespace.getRoomOperations(roomUuid).getClients()
            .stream()
            .filter(client -> {
              UserState userState = client.get(USER_STATE_KEY);
              return (userState != null && userState.isAdmin());
            })
            .forEach(client -> client.sendEvent(LLM_STATUS.name(), new LlmStatusDto(status)));
  }

  private UserState setUserState(String roomUuid, SocketIOClient client) {
    String userName = client.getHandshakeData().getSingleUrlParam(USER.getLabel());
    User user = roomService.getUser(UUID.fromString(roomUuid), userName);
    UserState userState = new UserState();
    userState.setId(user.getId());
    userState.setUsername(userName);
    userState.setAdmin(user.isAdmin());
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
    messagesProducer.publishRoomEvent(roomUuid, NEW_EDITOR_STATE, dto, null);
  }

  private void textUpdateSendAllEventHandler(SocketIOClient client, TextUpdateDto data, AckRequest ackSender) {
    String roomUuid = client.getHandshakeData().getSingleUrlParam(ROOM_UUID.getLabel());
    LOGGER.info("In room with UUID={} user={} updated text", roomUuid, data.getUsername());
    messagesProducer.publishRoomEvent(roomUuid, TEXT_UPDATE_SEND_ALL, data, null);
  }

  private void changeTemplateInRoom(Room room, SocketIONamespace namespace, String username) {
    LOGGER.info("Broadcast room UUID {}. with language {} and text {}", room.getRoomUuid(), room.getEditorLanguage(), room.getEditorText());
    TextUpdateDto textUpdateDto = new TextUpdateDto();
    textUpdateDto.setUsername(username);

    ChangeDto change = new ChangeDto();
    change.setRange(new RangeDto(START_POS_FOR_TEMPLATE, START_POS_FOR_TEMPLATE, END_POS_FOR_TEMPLATE, END_POS_FOR_TEMPLATE));
    change.setText(room.getEditorText());
    change.setVersion(1);
    textUpdateDto.setChanges(List.of(change));
    namespace.getRoomOperations(String.valueOf(room.getRoomUuid())).sendEvent(TEXT_UPDATE.name(), textUpdateDto);
  }
}
