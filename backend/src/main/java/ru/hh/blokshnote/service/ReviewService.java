package ru.hh.blokshnote.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hh.blokshnote.dto.comment.response.CommentDto;
import ru.hh.blokshnote.dto.review.request.CreateReviewDto;
import ru.hh.blokshnote.entity.Room;
import ru.hh.blokshnote.handler.RoomSocketHandler;
import ru.hh.blokshnote.utility.security.RoomSecurityUtils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class ReviewService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReviewService.class);

  private final CommentService commentService;
  private final RoomService roomService;
  private final LlmService llmService;
  private final RoomSocketHandler roomSocketHandler;
  private final ScheduledExecutorService sharedScheduledExecutorService;

  public ReviewService(
          CommentService commentService,
          RoomService roomService,
          LlmService llmService,
          RoomSocketHandler roomSocketHandler,
          ScheduledExecutorService sharedScheduledExecutorService
  ) {
    this.commentService = commentService;
    this.roomService = roomService;
    this.llmService = llmService;
    this.roomSocketHandler = roomSocketHandler;
    this.sharedScheduledExecutorService = sharedScheduledExecutorService;
  }

  @Transactional
  public CompletableFuture<CommentDto> createReviewAsync(UUID roomUuid, UUID adminToken, CreateReviewDto request) {
    Room room = roomService.getRoomByUuid(roomUuid);
    RoomSecurityUtils.verifyAdminToken(room, adminToken);

    String editorText = room.getEditorText();
    String prompt = request.prompt().strip();

    roomSocketHandler.broadcastLLMStatusToAdmins(roomUuid, false);
    CompletableFuture<String> reviewFuture = llmService.getReviewResponseAsync(editorText, prompt);

    ScheduledFuture<?> statusFuture = sharedScheduledExecutorService.scheduleAtFixedRate(() -> {
              roomSocketHandler.broadcastLLMStatusToAdmins(roomUuid, false);
            }, 1, 1, TimeUnit.SECONDS
    );

    return reviewFuture.handle((content, throwable) -> {
      try {
        statusFuture.cancel(false);
      } catch (Exception ex) {
        LOGGER.warn("Failed to cancel status updates", ex);
      }

      roomSocketHandler.broadcastLLMStatusToAdmins(roomUuid, true);

      if (throwable != null) {
        throw new CompletionException(throwable);
      }

      return commentService.createReviewComment(room, content);
    }).exceptionally(ex -> {
      LOGGER.error("Review creation failed", ex);
      throw ex instanceof CompletionException ?
              (CompletionException) ex :
              new CompletionException(ex);
    });

  }
}
