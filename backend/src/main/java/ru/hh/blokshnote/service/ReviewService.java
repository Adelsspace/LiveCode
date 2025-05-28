package ru.hh.blokshnote.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hh.blokshnote.dto.comment.response.CommentDto;
import ru.hh.blokshnote.dto.review.request.CreateReviewDto;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.utility.security.RoomSecurityUtils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ReviewService {
  private final CommentService commentService;
  private final RoomService roomService;
  private final LlmService llmService;

  public ReviewService(
      CommentService commentService,
      RoomService roomService,
      LlmService llmService
  ) {
    this.commentService = commentService;
    this.roomService = roomService;
    this.llmService = llmService;
  }

  @Transactional
  public CompletableFuture<CommentDto> createReviewAsync(UUID roomUuid, UUID adminToken, CreateReviewDto request) {
    Room room = roomService.getRoomByUuid(roomUuid);
    RoomSecurityUtils.verifyAdminToken(room, adminToken);

    String editorText = room.getEditorText();
    String prompt = request.prompt().strip();

    return llmService.getReviewResponseAsync(editorText, prompt)
        .thenApply(content -> commentService.createReviewComment(room, content));
  }
}
