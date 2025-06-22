package ru.hh.blokshnote.unittesting;

import io.socket.client.IO;
import io.socket.client.Socket;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.entity.User;
import ru.hh.blokshnote.service.RoomService;
import static ru.hh.blokshnote.utility.WsMessageType.CLOSE_ROOM;
import static ru.hh.blokshnote.utility.WsMessageType.CURSOR_POSITION;
import static ru.hh.blokshnote.utility.WsMessageType.LANGUAGE_CHANGE;
import static ru.hh.blokshnote.utility.WsMessageType.NEW_EDITOR_STATE;
import static ru.hh.blokshnote.utility.WsMessageType.OPEN_ROOM;
import static ru.hh.blokshnote.utility.WsMessageType.TEXT_SELECTION;
import static ru.hh.blokshnote.utility.WsMessageType.TEXT_UPDATE;
import static ru.hh.blokshnote.utility.WsMessageType.USERS_UPDATE;
import static ru.hh.blokshnote.utility.WsMessageType.USER_ACTIVITY;

public class SocketIOIntegrationTest extends NoKafkaAbstractIntegrationTest {

  @MockitoBean
  private RoomService roomService;

  private static final int TIMEOUT = 1500;
  private final String senderName = "John";
  private final String receiverName = "Jane";
  private final String adminName = "John";
  private final String nonAdminName = "Jane";

  private static int socketIoPort;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    socketIoPort = findAvailablePort();
    registry.add("socketio.port", () -> socketIoPort);
  }

  private static int findAvailablePort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    } catch (IOException e) {
      throw new RuntimeException("Could not find available port", e);
    }
  }

  private void ensureSocketsClosed(Socket... sockets) {
    for (Socket socket : sockets) {
      if (socket != null && socket.connected()) {
        socket.disconnect();
      }
    }
  }

  @Test
  public void testClientConnects() throws URISyntaxException, JSONException, ExecutionException, InterruptedException {
    UUID roomUuid = UUID.randomUUID();
    String userName = "John";

    Room room = new Room();
    room.setEditorText("Hello world!");
    room.setEditorLanguage("javascript");

    Mockito.when(roomService.getUser(roomUuid, userName)).thenReturn(new User());
    Mockito.when(roomService.getRoomByUuid(roomUuid)).thenReturn(room);

    CompletableFuture<JSONObject> editorStateFuture = new CompletableFuture<>();
    CompletableFuture<JSONObject> usersUpdateFuture = new CompletableFuture<>();

    Socket socket = initClient(roomUuid, userName);
    socket.on(NEW_EDITOR_STATE.name(), args -> editorStateFuture.complete((JSONObject) args[0]));
    socket.on(USERS_UPDATE.name(), args -> usersUpdateFuture.complete((JSONObject) args[0]));
    socket.connect();
    JSONObject newEditorState = editorStateFuture.orTimeout(TIMEOUT, TimeUnit.MILLISECONDS).get();
    JSONArray usersStates = usersUpdateFuture.orTimeout(TIMEOUT, TimeUnit.MILLISECONDS).get().getJSONArray("usersStates");
    Assertions.assertThat(newEditorState.getString("text")).isEqualTo("Hello world!");
    Assertions.assertThat(newEditorState.getString("language")).isEqualTo("javascript");

    Assertions.assertThat(usersStates.length()).isEqualTo(1);
    JSONObject usersState = usersStates.getJSONObject(0);
    Assertions.assertThat(usersState.getString("username")).isEqualTo(userName);
    Assertions.assertThat(usersState.getBoolean("isActive")).isTrue();
    Assertions.assertThat(usersState.getBoolean("isAdmin")).isFalse();
    ensureSocketsClosed(socket);
  }

  @Test
  public void textSelectionTest() throws URISyntaxException, JSONException, ExecutionException, InterruptedException {
    UUID roomUuid = UUID.randomUUID();
    Socket sender = initClient(roomUuid, senderName);
    Socket receiver = initClient(roomUuid, receiverName);
    clientsConnect(roomUuid, sender, receiver);

    CompletableFuture<JSONObject> textSelectionFuture = new CompletableFuture<>();
    receiver.on(TEXT_SELECTION.name(), args -> textSelectionFuture.complete((JSONObject) args[0]));

    JSONObject selection = new JSONObject();
    selection.put("startLineNumber", 1);
    selection.put("endLineNumber", 2);
    selection.put("startColumn", 1);
    selection.put("endColumn", 2);

    JSONObject textSelection = new JSONObject();
    textSelection.put("selection", selection);
    textSelection.put("username", senderName);

    sender.emit(TEXT_SELECTION.name(), textSelection);

    var result = textSelectionFuture.orTimeout(TIMEOUT, TimeUnit.MILLISECONDS).get();

    Assertions.assertThat(result.getString("username")).isEqualTo(senderName);
    Assertions.assertThat(result.getJSONObject("selection")).isNotNull();

    var resultSelection = result.getJSONObject("selection");

    Assertions.assertThat(resultSelection.getInt("startLineNumber")).isEqualTo(1);
    Assertions.assertThat(resultSelection.getInt("endLineNumber")).isEqualTo(2);
    Assertions.assertThat(resultSelection.getInt("startColumn")).isEqualTo(1);
    Assertions.assertThat(resultSelection.getInt("endColumn")).isEqualTo(2);

    ensureSocketsClosed(sender, receiver);
  }

  @Test
  public void cursorPositionTest() throws URISyntaxException, JSONException, ExecutionException, InterruptedException {
    UUID roomUuid = UUID.randomUUID();
    Socket sender = initClient(roomUuid, senderName);
    Socket receiver = initClient(roomUuid, receiverName);
    clientsConnect(roomUuid, sender, receiver);

    CompletableFuture<JSONObject> cursorPositionFuture = new CompletableFuture<>();
    receiver.on(CURSOR_POSITION.name(), args -> cursorPositionFuture.complete((JSONObject) args[0]));

    JSONObject positionDto = new JSONObject();
    positionDto.put("lineNumber", 1);
    positionDto.put("column", 2);

    JSONObject cursorPosition = new JSONObject();
    cursorPosition.put("position", positionDto);
    cursorPosition.put("username", senderName);

    sender.emit(CURSOR_POSITION.name(), cursorPosition);

    var result = cursorPositionFuture.orTimeout(TIMEOUT, TimeUnit.MILLISECONDS).get();

    Assertions.assertThat(result.getString("username")).isEqualTo(senderName);

    var resultPosition = result.getJSONObject("position");

    Assertions.assertThat(resultPosition).isNotNull();
    Assertions.assertThat(resultPosition.getInt("lineNumber")).isEqualTo(1);
    Assertions.assertThat(resultPosition.getInt("column")).isEqualTo(2);

    ensureSocketsClosed(sender, receiver);
  }

  @Test
  public void userActivityTest() throws URISyntaxException, JSONException, ExecutionException, InterruptedException {
    UUID roomUuid = UUID.randomUUID();
    Socket sender = initClient(roomUuid, senderName);
    Socket receiver = initClient(roomUuid, receiverName);
    clientsConnect(roomUuid, sender, receiver);

    CompletableFuture<JSONObject> userActivityFuture = new CompletableFuture<>();
    receiver.on(USER_ACTIVITY.name(), args -> userActivityFuture.complete((JSONObject) args[0]));

    JSONObject userActivity = new JSONObject();
    userActivity.put("isActive", false);
    userActivity.put("username", senderName);

    sender.emit(USER_ACTIVITY.name(), userActivity);

    var result = userActivityFuture.orTimeout(TIMEOUT, TimeUnit.MILLISECONDS).get();

    Assertions.assertThat(result.getString("username")).isEqualTo(senderName);
    Assertions.assertThat(result.getBoolean("isActive")).isFalse();

    ensureSocketsClosed(sender, receiver);
  }

  @Test
  public void languageChangeTestModifiedRoom() throws URISyntaxException, JSONException, ExecutionException, InterruptedException {
    UUID roomUuid = UUID.randomUUID();
    Socket sender = initClient(roomUuid, senderName);
    Socket receiver = initClient(roomUuid, receiverName);
    clientsConnect(roomUuid, sender, receiver);

    Room room = new Room();
    room.setModifiedByWritingCode(true);

    CompletableFuture<JSONObject> languageChangeFuture = new CompletableFuture<>();
    receiver.on(LANGUAGE_CHANGE.name(), args -> languageChangeFuture.complete((JSONObject) args[0]));

    String language = "java";
    JSONObject languageChange = new JSONObject();
    languageChange.put("language", language);
    languageChange.put("username", senderName);

    Mockito.when(roomService.updateRoomEditorLanguage(roomUuid, language)).thenReturn(room);

    sender.emit(LANGUAGE_CHANGE.name(), languageChange);

    var result = languageChangeFuture.orTimeout(TIMEOUT, TimeUnit.MILLISECONDS).get();

    Assertions.assertThat(result.getString("username")).isEqualTo(senderName);
    Assertions.assertThat(result.getString("language")).isEqualTo(language);

    ensureSocketsClosed(sender, receiver);
  }

  @Test
  public void textUpdateTest() throws URISyntaxException, JSONException, ExecutionException, InterruptedException {
    UUID roomUuid = UUID.randomUUID();
    Socket sender = initClient(roomUuid, senderName);
    Socket receiver = initClient(roomUuid, receiverName);
    clientsConnect(roomUuid, sender, receiver);

    CompletableFuture<JSONObject> textUpdateFuture = new CompletableFuture<>();
    receiver.on(TEXT_UPDATE.name(), args -> textUpdateFuture.complete((JSONObject) args[0]));

    JSONObject rangeDto = new JSONObject();
    rangeDto.put("startLineNumber", 1);
    rangeDto.put("endLineNumber", 2);
    rangeDto.put("startColumn", 1);
    rangeDto.put("endColumn", 2);

    JSONObject changeDto = new JSONObject();
    changeDto.put("range", rangeDto);
    changeDto.put("text", "Hello world!");
    changeDto.put("forceMoveMarkers", false);
    changeDto.put("version", 1);

    JSONArray changes = new JSONArray();
    changes.put(changeDto);

    JSONObject textUpdateDto = new JSONObject();
    changeDto.put("changes", changes);
    changeDto.put("username", senderName);

    sender.emit(TEXT_UPDATE.name(), textUpdateDto);

    var result = textUpdateFuture.orTimeout(TIMEOUT, TimeUnit.MILLISECONDS).get();

    Assertions.assertThat(result).usingRecursiveComparison().isEqualTo(textUpdateDto);

    ensureSocketsClosed(sender, receiver);
  }

  @Test
  public void testSecondClientConnectAndDisconnect() throws URISyntaxException, JSONException, InterruptedException {
    UUID roomUuid = UUID.randomUUID();
    String nameStayConnected = "John";
    String nameToDisconnected = "Jane";
    Map<String, JSONObject> usersStates = new ConcurrentHashMap<>();

    Mockito.when(roomService.getUser(roomUuid, nameStayConnected)).thenReturn(new User());
    Mockito.when(roomService.getUser(roomUuid, nameToDisconnected)).thenReturn(new User());
    Mockito.when(roomService.getRoomByUuid(roomUuid)).thenReturn(new Room());

    int firstConnectCounterLimit = 1;
    CountDownLatch firstConnect = new CountDownLatch(firstConnectCounterLimit);
    int secondConnectCounterLimit = 3;
    CountDownLatch secondConnect = new CountDownLatch(secondConnectCounterLimit);
    int disconnectCounterLimit = 4;
    CountDownLatch disconnect = new CountDownLatch(disconnectCounterLimit);

    Socket clientStayConnected = initClient(roomUuid, nameStayConnected);
    Socket clientToDisconnect = initClient(roomUuid, nameToDisconnected);

    clientToDisconnect.on(USERS_UPDATE.name(), args -> {
      usersStates.put(nameToDisconnected, (JSONObject) args[0]);
      firstConnect.countDown();
      secondConnect.countDown();
      disconnect.countDown();
    });
    clientStayConnected.on(USERS_UPDATE.name(), args -> {
      usersStates.put(nameStayConnected, (JSONObject) args[0]);
      firstConnect.countDown();
      secondConnect.countDown();
      disconnect.countDown();
    });

    clientToDisconnect.connect();
    Assertions.assertThat(firstConnect.await(TIMEOUT, TimeUnit.MILLISECONDS)).isTrue();
    Assertions.assertThat(usersStates.get(nameStayConnected)).isNull();
    var satesAfterFirstConnect = usersStates.get(nameToDisconnected).getJSONArray("usersStates");
    Assertions.assertThat(satesAfterFirstConnect.length()).isEqualTo(1);
    var janeStateAfterConnect = satesAfterFirstConnect.getJSONObject(0);
    Assertions.assertThat(janeStateAfterConnect.getString("username")).isEqualTo(nameToDisconnected);
    Assertions.assertThat(janeStateAfterConnect.getBoolean("isActive")).isTrue();
    Assertions.assertThat(janeStateAfterConnect.getBoolean("isAdmin")).isFalse();

    clientStayConnected.connect();
    Assertions.assertThat(secondConnect.await(TIMEOUT, TimeUnit.MILLISECONDS)).isTrue();
    Assertions.assertThat(usersStates.get(nameStayConnected).getJSONArray("usersStates").length()).isEqualTo(2);
    Assertions.assertThat(usersStates.get(nameToDisconnected).getJSONArray("usersStates").length()).isEqualTo(2);

    clientToDisconnect.disconnect();
    Assertions.assertThat(disconnect.await(TIMEOUT, TimeUnit.MILLISECONDS)).isTrue();
    Assertions.assertThat(usersStates.get(nameToDisconnected).getJSONArray("usersStates").length()).isEqualTo(2);

    var satesAfterDisconnect = usersStates.get(nameStayConnected).getJSONArray("usersStates");
    Assertions.assertThat(satesAfterDisconnect.length()).isEqualTo(1);
    var johnState = satesAfterDisconnect.getJSONObject(0);
    Assertions.assertThat(johnState.getString("username")).isEqualTo(nameStayConnected);
    Assertions.assertThat(johnState.getBoolean("isActive")).isTrue();
    Assertions.assertThat(johnState.getBoolean("isAdmin")).isFalse();

    ensureSocketsClosed(clientStayConnected);
  }

  @Test
  public void closeRoomByAdminTest() throws URISyntaxException, JSONException, ExecutionException, InterruptedException {
    UUID roomUuid = UUID.randomUUID();
    Socket adminClient = initClient(roomUuid, adminName);
    Socket nonAdminClient = initClient(roomUuid, nonAdminName);
    clientsConnect(roomUuid, adminClient, nonAdminClient);

    CompletableFuture<JSONObject> adminCloseRoomFuture = new CompletableFuture<>();
    CompletableFuture<JSONObject> nonAdminCloseRoomFuture = new CompletableFuture<>();
    adminClient.on(CLOSE_ROOM.name(), args -> adminCloseRoomFuture.complete((JSONObject) args[0]));
    nonAdminClient.on(CLOSE_ROOM.name(), args -> nonAdminCloseRoomFuture.complete((JSONObject) args[0]));

    JSONObject closeRoomDto = new JSONObject();
    closeRoomDto.put("username", adminName);

    adminClient.emit(CLOSE_ROOM.name(), closeRoomDto);

    var adminResult = nonAdminCloseRoomFuture.orTimeout(TIMEOUT, TimeUnit.MILLISECONDS).get();
    var nonAdminResult = nonAdminCloseRoomFuture.orTimeout(TIMEOUT, TimeUnit.MILLISECONDS).get();

    Assertions.assertThat(adminResult).usingRecursiveComparison().isEqualTo(closeRoomDto);
    Assertions.assertThat(nonAdminResult).usingRecursiveComparison().isEqualTo(closeRoomDto);
    Assertions.assertThat(nonAdminClient.connected()).isFalse();
    Assertions.assertThat(adminClient.connected()).isTrue();

    ensureSocketsClosed(adminClient, nonAdminClient);
  }

  @Test
  public void closeRoomByNonAdminTest() throws URISyntaxException, JSONException, ExecutionException, InterruptedException {
    UUID roomUuid = UUID.randomUUID();
    Socket adminClient = initClient(roomUuid, adminName);
    Socket nonAdminClient = initClient(roomUuid, nonAdminName);
    clientsConnect(roomUuid, adminClient, nonAdminClient);

    CompletableFuture<JSONObject> adminCloseRoomFuture = new CompletableFuture<>();
    CompletableFuture<JSONObject> nonAdminCloseRoomFuture = new CompletableFuture<>();
    adminClient.on(CLOSE_ROOM.name(), args -> adminCloseRoomFuture.complete((JSONObject) args[0]));
    nonAdminClient.on(CLOSE_ROOM.name(), args -> nonAdminCloseRoomFuture.complete((JSONObject) args[0]));

    JSONObject closeRoomDto = new JSONObject();
    closeRoomDto.put("username", adminName);

    nonAdminClient.emit(CLOSE_ROOM.name(), closeRoomDto);

    Assertions.assertThatThrownBy(() -> nonAdminCloseRoomFuture.orTimeout(TIMEOUT, TimeUnit.MILLISECONDS).get())
        .isInstanceOf(ExecutionException.class);
    Assertions.assertThatThrownBy(() -> adminCloseRoomFuture.orTimeout(TIMEOUT, TimeUnit.MILLISECONDS).get())
        .isInstanceOf(ExecutionException.class);
    Assertions.assertThat(nonAdminClient.connected()).isTrue();
    Assertions.assertThat(adminClient.connected()).isTrue();

    ensureSocketsClosed(adminClient, nonAdminClient);
  }

  @Test
  public void openRoomByAdminTest() throws URISyntaxException, JSONException, ExecutionException, InterruptedException {
    UUID roomUuid = UUID.randomUUID();

    var adminUser = new User();
    adminUser.setAdmin(true);
    Mockito.when(roomService.getUser(roomUuid, adminName)).thenReturn(adminUser);
    Mockito.when(roomService.getRoomByUuid(roomUuid)).thenReturn(new Room());

    int adminConnectCounterLimit = 1;
    CountDownLatch adminConnect = new CountDownLatch(adminConnectCounterLimit);
    CompletableFuture<JSONObject> adminOpenRoomFuture = new CompletableFuture<>();

    Socket adminClient = initClient(roomUuid, adminName);

    adminClient.on(USERS_UPDATE.name(), args -> adminConnect.countDown());

    adminClient.on(OPEN_ROOM.name(), args -> adminOpenRoomFuture.complete((JSONObject) args[0]));

    adminClient.connect();
    Assertions.assertThat(adminConnect.await(TIMEOUT, TimeUnit.MILLISECONDS)).isTrue();

    JSONObject openRoomDto = new JSONObject();
    openRoomDto.put("username", adminName);
    adminClient.emit(OPEN_ROOM.name(), openRoomDto);

    var adminResult = adminOpenRoomFuture.orTimeout(TIMEOUT, TimeUnit.MILLISECONDS).get();

    Assertions.assertThat(adminResult).usingRecursiveComparison().isEqualTo(openRoomDto);

    ensureSocketsClosed(adminClient);
  }

  @Test
  public void openRoomByNonAdminTest() throws URISyntaxException, JSONException, InterruptedException {
    UUID roomUuid = UUID.randomUUID();

    Mockito.when(roomService.getUser(roomUuid, nonAdminName)).thenReturn(new User());
    Mockito.when(roomService.getRoomByUuid(roomUuid)).thenReturn(new Room());

    int nonAdminConnectCounterLimit = 1;
    CountDownLatch nonAdminConnect = new CountDownLatch(nonAdminConnectCounterLimit);
    CompletableFuture<JSONObject> nonAdminOpenRoomFuture = new CompletableFuture<>();

    Socket nonAdminClient = initClient(roomUuid, nonAdminName);

    nonAdminClient.on(USERS_UPDATE.name(), args -> nonAdminConnect.countDown());

    nonAdminClient.on(OPEN_ROOM.name(), args -> nonAdminOpenRoomFuture.complete((JSONObject) args[0]));

    nonAdminClient.connect();
    Assertions.assertThat(nonAdminConnect.await(TIMEOUT, TimeUnit.MILLISECONDS)).isTrue();

    JSONObject openRoomDto = new JSONObject();
    openRoomDto.put("username", nonAdminName);
    nonAdminClient.emit(OPEN_ROOM.name(), openRoomDto);

    Assertions.assertThatThrownBy(() -> nonAdminOpenRoomFuture.orTimeout(TIMEOUT, TimeUnit.MILLISECONDS).get())
        .isInstanceOf(ExecutionException.class);

    ensureSocketsClosed(nonAdminClient);
  }

  @Test
  public void testConnectionAdminToClosedRoom() throws URISyntaxException, InterruptedException {
    UUID roomUuid = UUID.randomUUID();

    var adminUser = new User();
    adminUser.setAdmin(true);
    Mockito.when(roomService.getUser(roomUuid, adminName)).thenReturn(adminUser);
    Mockito.when(roomService.getRoomByUuid(roomUuid)).thenReturn(new Room());
    Mockito.when(roomService.isRoomClosed(roomUuid)).thenReturn(true);

    int adminConnectCounterLimit = 1;
    CountDownLatch adminConnect = new CountDownLatch(adminConnectCounterLimit);

    Socket adminClient = initClient(roomUuid, adminName);

    adminClient.on(USERS_UPDATE.name(), args -> adminConnect.countDown());

    adminClient.connect();
    Assertions.assertThat(adminConnect.await(TIMEOUT, TimeUnit.MILLISECONDS)).isTrue();
    Assertions.assertThat(adminClient.connected()).isTrue();
    ensureSocketsClosed(adminClient);
  }

  @Test
  public void testConnectionNonAdminToClosedRoom() throws URISyntaxException, JSONException, InterruptedException {
    UUID roomUuid = UUID.randomUUID();

    Mockito.when(roomService.getUser(roomUuid, nonAdminName)).thenReturn(new User());
    Mockito.when(roomService.isRoomClosed(roomUuid)).thenReturn(true);

    int nonAdminConnectCounterLimit = 1;
    CountDownLatch nonAdminConnect = new CountDownLatch(nonAdminConnectCounterLimit);

    Socket nonAdminClient = initClient(roomUuid, nonAdminName);

    nonAdminClient.on(USERS_UPDATE.name(), args -> nonAdminConnect.countDown());

    nonAdminClient.connect();
    Assertions.assertThat(nonAdminConnect.await(TIMEOUT, TimeUnit.MILLISECONDS)).isFalse();
    Assertions.assertThat(nonAdminClient.connected()).isFalse();
    ensureSocketsClosed(nonAdminClient);
  }

  private Socket initClient(UUID roomUuid, String userName) throws URISyntaxException {
    IO.Options options = new IO.Options();
    options.reconnection = false;
    options.forceNew = true;
    options.timeout = 5000;

    String url = "http://localhost:%d/ws/room/connect?roomUuid=%s&user=%s".formatted(socketIoPort, roomUuid, userName);
    return IO.socket(url, options);
  }

  private void clientsConnect(UUID roomUuid, Socket sender, Socket receiver) throws ExecutionException, InterruptedException {
    var admin = new User();
    admin.setAdmin(true);
    Mockito.when(roomService.getUser(roomUuid, senderName)).thenReturn(admin);
    Mockito.when(roomService.getUser(roomUuid, receiverName)).thenReturn(new User());
    Mockito.when(roomService.getRoomByUuid(roomUuid)).thenReturn(new Room());

    CompletableFuture<JSONObject> senderEditorStateFuture = new CompletableFuture<>();
    CompletableFuture<JSONObject> receiverEditorStateFuture = new CompletableFuture<>();

    receiver.on(NEW_EDITOR_STATE.name(), args -> receiverEditorStateFuture.complete((JSONObject) args[0]));
    sender.on(NEW_EDITOR_STATE.name(), args -> senderEditorStateFuture.complete((JSONObject) args[0]));

    receiver.connect();
    sender.connect();
    receiverEditorStateFuture.orTimeout(TIMEOUT, TimeUnit.MILLISECONDS).get();
    senderEditorStateFuture.orTimeout(TIMEOUT, TimeUnit.MILLISECONDS).get();
  }
}
