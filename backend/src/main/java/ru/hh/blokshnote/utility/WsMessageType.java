package ru.hh.blokshnote.utility;

import ru.hh.blokshnote.dto.review.request.LlmStatusDto;
import ru.hh.blokshnote.dto.websocket.ClosingRoomDto;
import ru.hh.blokshnote.dto.websocket.CursorPositionDto;
import ru.hh.blokshnote.dto.websocket.EditorStateDto;
import ru.hh.blokshnote.dto.websocket.LanguageChangeDto;
import ru.hh.blokshnote.dto.websocket.NewCommentDto;
import ru.hh.blokshnote.dto.websocket.OpeningRoomDto;
import ru.hh.blokshnote.dto.websocket.TextSelectionDto;
import ru.hh.blokshnote.dto.websocket.TextUpdateDto;
import ru.hh.blokshnote.dto.websocket.UserActivityDto;
import ru.hh.blokshnote.dto.websocket.UsersUpdateDto;

public enum WsMessageType {
  NEW_EDITOR_STATE(EditorStateDto.class),
  TEXT_SELECTION(TextSelectionDto.class),
  CURSOR_POSITION(CursorPositionDto.class),
  USER_ACTIVITY(UserActivityDto.class),
  LANGUAGE_CHANGE(LanguageChangeDto.class),
  TEXT_UPDATE(TextUpdateDto.class),
  USERS_UPDATE(UsersUpdateDto.class),
  NEW_COMMENT(NewCommentDto.class),
  CLOSE_ROOM(ClosingRoomDto.class),
  OPEN_ROOM(OpeningRoomDto.class),
  NEW_EDITOR_STATE_SEND_ALL(EditorStateDto.class),
  TEXT_UPDATE_SEND_ALL(TextUpdateDto.class),
  LLM_STATUS(LlmStatusDto.class),
  ;

  private final Class<?> dtoClass;

  WsMessageType(Class<?> dtoClass) {
    this.dtoClass = dtoClass;
  }

  public Class<?> getDtoClass() {
    return this.dtoClass;
  }
}
