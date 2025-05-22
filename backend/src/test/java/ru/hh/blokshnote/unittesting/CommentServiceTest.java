package ru.hh.blokshnote.unittesting;

import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.blokshnote.dto.comment.request.CreateCommentDto;
import ru.hh.blokshnote.dto.comment.response.CommentDto;
import ru.hh.blokshnote.dto.comment.response.RoomCommentsResponse;
import ru.hh.blokshnote.dto.room.request.CreateRoomRequest;
import ru.hh.blokshnote.entity.Comment;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.repository.CommentRepository;
import ru.hh.blokshnote.service.CommentService;
import ru.hh.blokshnote.service.RoomService;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CommentServiceTest extends AbstractIntegrationTest {
  @Autowired
  RoomService roomService;

  @Autowired
  CommentService commentService;

  @Autowired
  CommentRepository commentRepository;

  private Room testRoom;
  private final UUID testRoomUuid = UUID.randomUUID();
  private final String adminUsername = "admin";

  @BeforeEach
  public void setUp() {
    CreateRoomRequest request = new CreateRoomRequest();
    request.setUuid(testRoomUuid);
    request.setUsername(adminUsername);
    testRoom = roomService.createRoomWithAdmin(request);
  }

  @Test
  void testAddNewCommentByAdmin() {
    String message = "Hello from admin";
    CreateCommentDto commentDto = new CreateCommentDto(message, adminUsername);

    CommentDto result = commentService.createComment(testRoomUuid, testRoom.getAdminToken(), commentDto);

    assertNotNull(result);
    assertEquals(message, result.content());
    assertEquals(adminUsername, result.author());
    assertEquals(testRoomUuid, result.roomUuid());
    assertFalse(result.isLlm());
    assertNotNull(result.createdAt());

    List<Comment> commentsInDb = commentRepository.findAllByRoomOrderByCreatedAtAsc(testRoom);
    assertEquals(1, commentsInDb.size());
    assertEquals(message, commentsInDb.get(0).getContent());
  }

  @Test
  void testTryToAddCommentToNotExistingRoom() {
    UUID tempRoomUuid = UUID.randomUUID();
    while (tempRoomUuid.equals(testRoomUuid)) {
      tempRoomUuid = UUID.randomUUID();
    }

    UUID fakeRoomUuid = tempRoomUuid;
    UUID fakeAdminToken = UUID.randomUUID();
    CreateCommentDto commentDto = new CreateCommentDto("Should fail", adminUsername);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> commentService.createComment(fakeRoomUuid, fakeAdminToken, commentDto)
    );
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertNotNull(exception.getReason());
    assertTrue(exception.getReason().contains("Room with UUID"));
    List<Comment> commentsInDb = commentRepository.findAllByRoomOrderByCreatedAtAsc(testRoom);
    assertEquals(0, commentsInDb.size());
  }

  @Test
  void testTryToAddCommentInvalidAdminToken() {
    UUID tempToken = UUID.randomUUID();
    while (tempToken.equals(testRoom.getAdminToken())) {
      tempToken = UUID.randomUUID();
    }

    UUID invalidAdminToken = tempToken;
    CreateCommentDto commentDto = new CreateCommentDto("Invalid token attempt", adminUsername);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> commentService.createComment(testRoomUuid, invalidAdminToken, commentDto)
    );

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertNotNull(exception.getReason());
    assertTrue(exception.getReason().contains("Invalid admin token"));
    List<Comment> commentsInDb = commentRepository.findAllByRoomOrderByCreatedAtAsc(testRoom);
    assertEquals(0, commentsInDb.size());
  }

  @Test
  void testTryToAddCommentByNotExistingAdminUsername() {
    String unknownUsername = "ghost";
    CreateCommentDto commentDto = new CreateCommentDto("Should not be saved", unknownUsername);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> commentService.createComment(testRoomUuid, testRoom.getAdminToken(), commentDto)
    );

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertNotNull(exception.getReason());
    assertTrue(exception.getReason().contains("Admin with this username not found in room"));
    List<Comment> commentsInDb = commentRepository.findAllByRoomOrderByCreatedAtAsc(testRoom);
    assertEquals(0, commentsInDb.size());
  }

  @Test
  void testGetAllCommentsInRoom() {
    commentService.createComment(testRoomUuid, testRoom.getAdminToken(), new CreateCommentDto("First comment", adminUsername));
    commentService.createComment(testRoomUuid, testRoom.getAdminToken(), new CreateCommentDto("Second comment", adminUsername));

    RoomCommentsResponse response = commentService.getAllCommentsInRoom(testRoomUuid, testRoom.getAdminToken());

    List<CommentDto> comments = response.items();
    assertEquals(2, comments.size());
    assertEquals("First comment", comments.get(0).content());
    assertEquals("Second comment", comments.get(1).content());
    assertEquals(adminUsername, comments.get(0).author());
    assertEquals(adminUsername, comments.get(1).author());
  }

  @Test
  void testTryToGetAllCommentsInvalidAdminToken() {
    UUID tempToken = UUID.randomUUID();
    while (tempToken.equals(testRoom.getAdminToken())) {
      tempToken = UUID.randomUUID();
    }

    UUID invalidAdminToken = tempToken;

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> commentService.getAllCommentsInRoom(testRoomUuid, invalidAdminToken)
    );

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertNotNull(exception.getReason());
    assertTrue(exception.getReason().contains("Invalid admin token"));
  }
}
