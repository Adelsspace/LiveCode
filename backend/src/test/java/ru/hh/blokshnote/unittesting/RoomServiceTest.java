package ru.hh.blokshnote.unittesting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.blokshnote.dto.room.request.CreateRoomRequest;
import ru.hh.blokshnote.dto.user.request.CreateUserRequest;
import ru.hh.blokshnote.dto.websocket.EditorStateDto;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.entity.User;
import ru.hh.blokshnote.service.RoomService;
import ru.hh.blokshnote.utility.LanguagesInRoom;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
public class RoomServiceTest extends NoKafkaAbstractIntegrationTest {
  @Autowired
  RoomService roomService;

  private Room testRoom;
  private final UUID testRoomUuid = UUID.randomUUID();
  private final String adminUsername = "admin";

  @BeforeEach
  public void setUp() {
    CreateRoomRequest request = new CreateRoomRequest();
    request.setUuid(testRoomUuid);

    testRoom = roomService.createRoom(request);
    CreateUserRequest userRequest = new CreateUserRequest();
    userRequest.setUsername(adminUsername);
    roomService.addAdminToRoom(testRoom.getRoomUuid(), testRoom.getAdminToken(), userRequest);
  }

  @Test
  void getExistingRoomByUuid() {
    Room existingRoom = roomService.getRoomByUuid(testRoomUuid);

    assertNotNull(existingRoom);
    assertEquals(testRoomUuid, existingRoom.getRoomUuid());
    assertEquals(testRoom.getAdminToken(), existingRoom.getAdminToken());
    assertEquals(testRoom.getCreatedAt(), existingRoom.getCreatedAt());
    assertEquals(testRoom.getExpiredAt(), existingRoom.getExpiredAt());
  }

  @Test
  public void testThrowNotFoundExceptionIfRoomDoesNotExist() {
    UUID tempUuid = UUID.randomUUID();
    while (tempUuid.equals(testRoomUuid)) {
      tempUuid = UUID.randomUUID();
    }
    final UUID nonExistentRoomUuid = tempUuid;

    assertThatThrownBy(() -> roomService.getRoomByUuid(nonExistentRoomUuid))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Room with UUID=" + tempUuid + " not found")
        .matches(ex -> ((ResponseStatusException) ex).getStatusCode().equals(HttpStatus.NOT_FOUND));
  }

  @Test
  void testCreateRoom() {
    UUID roomUuid = UUID.randomUUID();

    CreateRoomRequest request = new CreateRoomRequest();
    request.setUuid(roomUuid);

    Room room = roomService.createRoom(request);

    assertNotNull(room);
    assertEquals(roomUuid, room.getRoomUuid());
    assertNotNull(room.getAdminToken());
    assertNotNull(room.getCreatedAt());
    assertNotNull(room.getExpiredAt());
  }

  @Test
  void testCreateRoomWithExistingUuid() {
    CreateRoomRequest request = new CreateRoomRequest();
    request.setUuid(testRoomUuid);

    Room room = roomService.createRoom(request);

    assertNotNull(room);
    assertEquals(testRoomUuid, room.getRoomUuid());
    assertNotNull(room.getAdminToken());
    assertNotNull(room.getCreatedAt());
    assertNotNull(room.getExpiredAt());
  }

  @Test
  public void testAddUserToRoom() {
    String username = "user1";
    CreateUserRequest userRequest = new CreateUserRequest();
    userRequest.setUsername(username);

    User user = roomService.addUserToRoom(testRoomUuid, userRequest);
    Room room = roomService.getRoomByUuid(testRoomUuid);

    assertNotNull(user);
    assertEquals(username, user.getName());
    assertFalse(user.isAdmin());
    assertEquals(room.getId(), user.getRoom().getId());
  }

  @Test
  void testAddExistingUsernameToRoom() {
    CreateUserRequest userRequest = new CreateUserRequest();
    userRequest.setUsername(adminUsername);

    assertThatThrownBy(() -> roomService.addUserToRoom(testRoomUuid, userRequest))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("User with this name already exists in the room")
        .matches(ex -> ((ResponseStatusException) ex).getStatusCode().equals(HttpStatus.CONFLICT));
  }

  @Test
  void testSuccessAddAdminToRoom() {
    UUID validAdminToken = testRoom.getAdminToken();
    String username = "new_admin";
    CreateUserRequest userRequest = new CreateUserRequest();
    userRequest.setUsername(username);

    User adminUser = roomService.addAdminToRoom(testRoomUuid, validAdminToken, userRequest);
    Room room = roomService.getRoomByUuid(testRoomUuid);

    assertNotNull(adminUser);
    assertEquals(username, adminUser.getName());
    assertTrue(adminUser.isAdmin());
    assertEquals(room.getId(), adminUser.getRoom().getId());
  }

  @Test
  void testInvalidAdminTokenAddition() {
    UUID tempUuid = UUID.randomUUID();
    while (tempUuid.equals(testRoom.getAdminToken())) {
      tempUuid = UUID.randomUUID();
    }
    final UUID invalidAdminToken = tempUuid;
    String username = "new_admin";
    CreateUserRequest userRequest = new CreateUserRequest();
    userRequest.setUsername(username);

    assertThatThrownBy(() -> roomService.addAdminToRoom(testRoomUuid, invalidAdminToken, userRequest))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Invalid admin token")
        .matches(ex -> ((ResponseStatusException) ex).getStatusCode().equals(HttpStatus.FORBIDDEN));
  }

  @Test
  void testAddExistingAdminUsernameToRoom() {
    UUID adminToken = testRoom.getAdminToken();
    CreateUserRequest userRequest = new CreateUserRequest();
    userRequest.setUsername(adminUsername);

    assertThatThrownBy(() -> roomService.addAdminToRoom(testRoomUuid, adminToken, userRequest))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("User with this name already exists in the room")
        .matches(ex -> ((ResponseStatusException) ex).getStatusCode().equals(HttpStatus.CONFLICT));
  }

  @Test
  void changeRoomTemplateWhenRoomExistsUpdatesEditorText() {
    String alias = "java";
    Room updated = roomService.updateRoomEditorLanguage(testRoomUuid, alias);

    assertNotNull(updated);
    assertEquals(testRoomUuid, updated.getRoomUuid());
    String expectedTemplate = LanguagesInRoom.getTemplateByAlias(alias);
    assertEquals(expectedTemplate, updated.getEditorText());
  }


  @Test
  void changeRoomTemplateWhenRoomNotFoundThrowsNotFound() {
    UUID nonExistent = UUID.randomUUID();
    assertThrows(ResponseStatusException.class, () -> {
      roomService.updateRoomEditorLanguage(nonExistent, "anyAlias");
    }, "Expected ResponseStatusException when changing template of non-existent room");
  }

  @Test
  void changeRoomLanguageWhenFlagIsFalseNotUsingTemplate() {
    Room before = roomService.getRoomByUuid(testRoomUuid);
    assertFalse(before.isModifiedByWritingCode());
    String testText = "Not template text";

    EditorStateDto dto = new EditorStateDto();
    dto.setLanguage("javascript");
    dto.setText(testText);
    roomService.updateRoomEditor(testRoomUuid, dto);
    roomService.updateRoomEditorLanguage(testRoomUuid, "java");

    Room after = roomService.getRoomByUuid(testRoomUuid);

    assertTrue(after.isModifiedByWritingCode());
    assertEquals(testText, after.getEditorText());
  }

  @Test
  void changeRoomTemplateWhenAliasNotFoundUsesPlainTemplate() {
    String invalidAlias = "nonexistent";
    Room updated = roomService.updateRoomEditorLanguage(testRoomUuid, invalidAlias);

    assertNotNull(updated);
    assertEquals(testRoomUuid, updated.getRoomUuid());
    String expectedTemplate = LanguagesInRoom.getTemplateByAlias(invalidAlias);
    assertEquals(expectedTemplate, updated.getEditorText());
  }
}
