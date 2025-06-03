package ru.hh.blokshnote.dto.comment.response;

import java.time.Instant;
import java.util.UUID;

import ru.hh.blokshnote.entity.Comment;

public record CommentDto(String content, UUID roomUuid, String author, String color, Instant createdAt, boolean isLlm) {
  public static CommentDto fromEntity(Comment comment) {
    String color = null;
    String author = null;
    if (comment.getUser() != null) {
      color = comment.getUser().getColor();
      author = comment.getUser().getName();
    }
    return new CommentDto(
        comment.getContent(),
        comment.getRoom().getRoomUuid(),
        author,
        color,
        comment.getCreatedAt(),
        comment.isLlm()
    );
  }
}
