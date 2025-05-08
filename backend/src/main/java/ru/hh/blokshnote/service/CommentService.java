package ru.hh.blokshnote.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.blokshnote.dto.comment.request.CreateCommentDto;
import ru.hh.blokshnote.dto.comment.response.CommentDto;
import ru.hh.blokshnote.dto.comment.response.RoomCommentsResponse;
import ru.hh.blokshnote.entity.Comment;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.entity.User;
import ru.hh.blokshnote.repository.CommentRepository;
import ru.hh.blokshnote.repository.UserRepository;

@Service
public class CommentService {
  private final UserRepository userRepository;
  private final CommentRepository commentRepository;
  private final RoomService roomService;
  private final RoomSecurityService roomSecurityService;

  public CommentService(
      UserRepository userRepository,
      CommentRepository commentRepository,
      RoomService roomService,
      RoomSecurityService roomSecurityService
  ) {
    this.userRepository = userRepository;
    this.commentRepository = commentRepository;
    this.roomService = roomService;
    this.roomSecurityService = roomSecurityService;
  }

  @Transactional
  public CommentDto createComment(UUID roomUuid, UUID adminToken, CreateCommentDto request) {
    Room room = roomService.getRoomByUuid(roomUuid);
    roomSecurityService.verifyAdminToken(room, adminToken);

    String username = request.author();
    String content = request.content();
    User admin = userRepository
        .findByNameAndRoom(username, room)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin with this username not found in room"));
    Comment comment = new Comment.Builder().content(content).room(room).user(admin).isLlm(false).createdAt(Instant.now()).build();
    commentRepository.save(comment);
    return CommentDto.fromEntity(comment);
  }

  @Transactional(readOnly = true)
  public RoomCommentsResponse getAllCommentsInRoom(UUID roomUuid, UUID adminToken) {
    Room room = roomService.getRoomByUuid(roomUuid);
    roomSecurityService.verifyAdminToken(room, adminToken);

    List<Comment> commentList = commentRepository.findAllByRoomOrderByCreatedAtAsc(room);
    List<CommentDto> comments = commentList.stream().map(CommentDto::fromEntity).toList();
    return new RoomCommentsResponse(comments);
  }
}
