package ru.hh.blokshnote.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hh.blokshnote.dto.comment.response.CommentDto;
import ru.hh.blokshnote.dto.review.request.CreateReviewDto;
import ru.hh.blokshnote.entity.Comment;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.repository.CommentRepository;
import ru.hh.blokshnote.utility.security.RoomSecurityUtils;

import java.util.UUID;

@Service
public class ReviewService {
  private final CommentRepository commentRepository;
  private final RoomService roomService;
  private final LlmService llmService;

  public ReviewService(
      CommentRepository commentRepository,
      RoomService roomService,
      LlmService llmService
  ) {
    this.commentRepository = commentRepository;
    this.roomService = roomService;
    this.llmService = llmService;
  }

  @Transactional
  public CommentDto createReview(UUID roomUuid, UUID adminToken, CreateReviewDto request) {
    Room room = roomService.getRoomByUuid(roomUuid);
    RoomSecurityUtils.verifyAdminToken(room, adminToken);

    String editorText = request.editorText();
    String prompt = request.prompt();

    String content = llmService.getReviewResponse(editorText, prompt);

    Comment comment = new Comment(content, room, null, true);
    commentRepository.save(comment);
    return CommentDto.fromEntity(comment);
  }
}
