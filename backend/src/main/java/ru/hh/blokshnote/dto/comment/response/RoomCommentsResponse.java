package ru.hh.blokshnote.dto.comment.response;

import java.util.List;

public record RoomCommentsResponse(List<CommentDto> items) {
}
