package ru.hh.blokshnote.service;

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
import ru.hh.blokshnote.handler.RoomSocketHandler;
import ru.hh.blokshnote.repository.CommentRepository;
import ru.hh.blokshnote.repository.UserRepository;
import ru.hh.blokshnote.utility.security.RoomSecurityUtils;

@Service
public class CommentService {
  private final UserRepository userRepository;
  private final CommentRepository commentRepository;
  private final RoomService roomService;
  private final RoomSocketHandler roomSocketHandler;

  public CommentService(
      UserRepository userRepository,
      CommentRepository commentRepository,
      RoomService roomService,
      RoomSocketHandler roomSocketHandler
  ) {
    this.userRepository = userRepository;
    this.commentRepository = commentRepository;
    this.roomService = roomService;
    this.roomSocketHandler = roomSocketHandler;
  }

  @Transactional
  public CommentDto createComment(UUID roomUuid, UUID adminToken, CreateCommentDto request) {
    Room room = roomService.getRoomByUuid(roomUuid);
    RoomSecurityUtils.verifyAdminToken(room, adminToken);

    String username = request.author();
    String content = request.content();
    User admin = userRepository
        .findByNameAndRoom(username, room)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin with this username not found in room"));
    Comment comment = new Comment(content, room, admin, false);
    commentRepository.save(comment);
    roomSocketHandler.broadcastNewCommentToAdmins(String.valueOf(roomUuid));
    return CommentDto.fromEntity(comment);
  }

  @Transactional(readOnly = true)
  public RoomCommentsResponse getAllCommentsInRoom(UUID roomUuid, UUID adminToken) {
    Room room = roomService.getRoomByUuid(roomUuid);
    RoomSecurityUtils.verifyAdminToken(room, adminToken);

    List<Comment> commentList = commentRepository.findAllByRoomOrderByCreatedAtAsc(room);
    List<CommentDto> comments = commentList.stream().map(CommentDto::fromEntity).toList();
    return new RoomCommentsResponse(comments);
  }

  @Transactional
  public CommentDto createReviewComment(Room room, String content) {
    Comment comment = new Comment(content, room, null, true);
    commentRepository.save(comment);
    roomSocketHandler.broadcastNewCommentToAdmins(String.valueOf(room.getRoomUuid()));
    return CommentDto.fromEntity(comment);
  }
}
