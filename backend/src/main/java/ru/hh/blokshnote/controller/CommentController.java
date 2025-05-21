package ru.hh.blokshnote.controller;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.hh.blokshnote.dto.comment.request.CreateCommentDto;
import ru.hh.blokshnote.dto.comment.response.CommentDto;
import ru.hh.blokshnote.dto.comment.response.RoomCommentsResponse;
import ru.hh.blokshnote.service.CommentService;

@RestController
@RequestMapping("/api/rooms")
public class CommentController {
  private final CommentService commentService;

  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  @PostMapping("/{uuid}/comments")
  public CommentDto createComment(
      @PathVariable("uuid") UUID roomUuid,
      @RequestParam("adminToken") UUID adminToken,
      @RequestBody CreateCommentDto request
  ) {
    return commentService.createComment(roomUuid, adminToken, request);
  }

  @GetMapping("/{uuid}/comments")
  public RoomCommentsResponse getAllCommentsInRoom(
      @PathVariable("uuid") UUID roomUuid,
      @RequestParam("adminToken") UUID adminToken
  ) {
    return commentService.getAllCommentsInRoom(roomUuid, adminToken);
  }
}
