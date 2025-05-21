package ru.hh.blokshnote.dto.comment.response;

import java.time.Instant;
import java.util.UUID;

import ru.hh.blokshnote.entity.Comment;

public record CommentDto(String content, UUID roomUuid, String author, Instant createdAt, boolean isLlm) {
  public static CommentDto fromEntity(Comment comment) {
    return new CommentDto(
        comment.getContent(),
        comment.getRoom().getRoomUuid(),
        comment.getUser() != null ? comment.getUser().getName() : null,
        comment.getCreatedAt(),
        comment.isLlm()
    );
  }
}
