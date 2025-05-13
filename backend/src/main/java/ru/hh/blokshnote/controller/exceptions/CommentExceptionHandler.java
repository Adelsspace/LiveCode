package ru.hh.blokshnote.controller.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.blokshnote.controller.CommentController;
import ru.hh.blokshnote.dto.error.ErrorResponse;

@RestControllerAdvice(assignableTypes = CommentController.class)
public class CommentExceptionHandler {

  @ExceptionHandler(ResponseStatusException.class)
  public Object handleResponseStatusException(ResponseStatusException ex) {
    String message = ex.getReason();
    if (message != null) {
      return ResponseEntity.status(ex.getStatusCode()).body(new ErrorResponse(message));
    }
    throw ex;
  }
}
