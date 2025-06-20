package ru.hh.blokshnote.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hh.blokshnote.dto.comment.response.CommentDto;
import ru.hh.blokshnote.dto.review.request.CreateReviewDto;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.handler.RoomSocketHandler;
import ru.hh.blokshnote.utility.security.RoomSecurityUtils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class ReviewService {
  private final CommentService commentService;
  private final RoomService roomService;
  private final LlmService llmService;
  private final RoomSocketHandler roomSocketHandler;

  public ReviewService(
      CommentService commentService,
      RoomService roomService,
      LlmService llmService,
      RoomSocketHandler roomSocketHandler
  ) {
    this.commentService = commentService;
    this.roomService = roomService;
    this.llmService = llmService;
    this.roomSocketHandler = roomSocketHandler;
  }

  @Transactional
  public CompletableFuture<CommentDto> createReviewAsync(UUID roomUuid, UUID adminToken, CreateReviewDto request) {
    Room room = roomService.getRoomByUuid(roomUuid);
    RoomSecurityUtils.verifyAdminToken(room, adminToken);

    String editorText = room.getEditorText();
    String prompt = request.prompt().strip();

    CompletableFuture<String> reviewFuture = llmService.getReviewResponseAsync(editorText, prompt);

    waitFutureAndSendLlmStatus(reviewFuture, roomUuid);

    return reviewFuture.thenApply(content -> commentService.createReviewComment(room, content));
  }

  private void waitFutureAndSendLlmStatus(CompletableFuture<String> reviewFuture, UUID roomUuid) {
    try (ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor()) {

      ex.scheduleAtFixedRate(() -> {
        roomSocketHandler.broadcastLLMStatusToAdmins(roomUuid, false);
      }, 0, 1, TimeUnit.SECONDS);

      reviewFuture.join();
      //ex.shutdownNow();
      CompletableFuture.runAsync(() -> roomSocketHandler.broadcastLLMStatusToAdmins(roomUuid, true));
    }
  }

}
