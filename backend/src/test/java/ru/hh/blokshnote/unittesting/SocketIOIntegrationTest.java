package ru.hh.blokshnote.unittesting;

import io.socket.client.IO;
import io.socket.client.Socket;
import org.assertj.core.api.Assertions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.entity.User;
import ru.hh.blokshnote.service.RoomService;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static ru.hh.blokshnote.utility.WsMessageType.CURSOR_POSITION;
import static ru.hh.blokshnote.utility.WsMessageType.LANGUAGE_CHANGE;
import static ru.hh.blokshnote.utility.WsMessageType.NEW_EDITOR_STATE;
import static ru.hh.blokshnote.utility.WsMessageType.TEXT_SELECTION;
import static ru.hh.blokshnote.utility.WsMessageType.TEXT_UPDATE;
import static ru.hh.blokshnote.utility.WsMessageType.USERS_UPDATE;
import static ru.hh.blokshnote.utility.WsMessageType.USER_ACTIVITY;

@SpringBootTest
public class SocketIOIntegrationTest extends AbstractIntegrationTest {

  @MockitoBean
  private RoomService roomService;

  private static final int TIMEOUT = 500;
  private final String senderName = "John";
  private final String receiverName = "Jane";

  @BeforeAll
  static void startServer() {
    System.setProperty("socketio.host", "localhost");
    System.setProperty("socketio.port", "9092");
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
    editorStateFuture.orTimeout(TIMEOUT, TimeUnit.MILLISECONDS);
    usersUpdateFuture.orTimeout(TIMEOUT, TimeUnit.MILLISECONDS);
    JSONObject newEditorState = editorStateFuture.get();
    Assertions.assertThat(newEditorState.getString("text")).isEqualTo("Hello world!");
    Assertions.assertThat(newEditorState.getString("language")).isEqualTo("javascript");

    JSONArray usersStates = usersUpdateFuture.get().getJSONArray("usersStates");
    Assertions.assertThat(usersStates.length()).isEqualTo(1);
    JSONObject usersState = usersStates.getJSONObject(0);
    Assertions.assertThat(usersState.getString("username")).isEqualTo(userName);
    Assertions.assertThat(usersState.getBoolean("isActive")).isTrue();
    Assertions.assertThat(usersState.getBoolean("isAdmin")).isFalse();
    socket.disconnect();
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

    sender.disconnect();
    receiver.disconnect();
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

    sender.disconnect();
    receiver.disconnect();
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

    sender.disconnect();
    receiver.disconnect();
  }

  @Test
  public void languageChangeTest() throws URISyntaxException, JSONException, ExecutionException, InterruptedException {
    UUID roomUuid = UUID.randomUUID();
    Socket sender = initClient(roomUuid, senderName);
    Socket receiver = initClient(roomUuid, receiverName);
    clientsConnect(roomUuid, sender, receiver);

    CompletableFuture<JSONObject> languageChangeFuture = new CompletableFuture<>();
    receiver.on(LANGUAGE_CHANGE.name(), args -> languageChangeFuture.complete((JSONObject) args[0]));

    String language = "Java";
    JSONObject languageChange = new JSONObject();
    languageChange.put("language", language);
    languageChange.put("username", senderName);

    sender.emit(LANGUAGE_CHANGE.name(), languageChange);

    var result = languageChangeFuture.orTimeout(TIMEOUT, TimeUnit.MILLISECONDS).get();

    Assertions.assertThat(result.getString("username")).isEqualTo(senderName);
    Assertions.assertThat(result.getString("language")).isEqualTo(language);

    sender.disconnect();
    receiver.disconnect();
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

    sender.disconnect();
    receiver.disconnect();
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

    clientStayConnected.disconnect();
  }

  private Socket initClient(UUID roomUuid, String userName) throws URISyntaxException {
    IO.Options options = new IO.Options();
    options.reconnection = false;
    options.forceNew = true;

    return IO.socket(
        "http://localhost:9092/ws/room/connect?roomUuid=%s&user=%s".formatted(roomUuid, userName),
        options
    );
  }

  private void clientsConnect(UUID roomUuid, Socket sender, Socket receiver) {
    Mockito.when(roomService.getUser(roomUuid, senderName)).thenReturn(new User());
    Mockito.when(roomService.getUser(roomUuid, receiverName)).thenReturn(new User());
    Mockito.when(roomService.getRoomByUuid(roomUuid)).thenReturn(new Room());

    CompletableFuture<JSONObject> senderEditorStateFuture = new CompletableFuture<>();
    CompletableFuture<JSONObject> receiverEditorStateFuture = new CompletableFuture<>();


    receiver.on(NEW_EDITOR_STATE.name(), args -> receiverEditorStateFuture.complete((JSONObject) args[0]));
    sender.on(NEW_EDITOR_STATE.name(), args -> senderEditorStateFuture.complete((JSONObject) args[0]));

    receiver.connect();
    sender.connect();
    receiverEditorStateFuture.orTimeout(TIMEOUT, TimeUnit.MILLISECONDS);
    senderEditorStateFuture.orTimeout(TIMEOUT, TimeUnit.MILLISECONDS);
  }
}
