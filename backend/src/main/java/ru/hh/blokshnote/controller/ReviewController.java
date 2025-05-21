package ru.hh.blokshnote.controller;


import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.hh.blokshnote.dto.comment.response.CommentDto;
import ru.hh.blokshnote.dto.review.request.CreateReviewDto;
import ru.hh.blokshnote.service.ReviewService;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/rooms")
public class ReviewController {
  private final ReviewService reviewService;

  public ReviewController(ReviewService reviewService) {
    this.reviewService = reviewService;
  }

  @PostMapping("/{uuid}/review")
  public CompletableFuture<CommentDto> createReviewAsync(
      @PathVariable("uuid") UUID roomUuid,
      @RequestParam("adminToken") UUID adminToken,
      @RequestBody CreateReviewDto request
  ) {
    return reviewService.createReviewAsync(roomUuid, adminToken, request);
  }
}
