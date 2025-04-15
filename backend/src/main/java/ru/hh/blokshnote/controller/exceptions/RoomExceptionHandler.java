package ru.hh.blokshnote.controller.exceptions;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.blokshnote.controller.RoomController;
import ru.hh.blokshnote.dto.error.ErrorResponse;


@RestControllerAdvice(assignableTypes = RoomController.class)
public class RoomExceptionHandler {

  private static final Map<HttpStatus, String> ERROR_MESSAGES = Map.of(
      HttpStatus.NOT_FOUND, "Room does not exist",
      HttpStatus.CONFLICT, "User with this name already exists",
      HttpStatus.FORBIDDEN, "Invalid admin token"
  );

  @ExceptionHandler(ResponseStatusException.class)
  public Object handleResponseStatusException(ResponseStatusException ex) {
    HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
    String message = ERROR_MESSAGES.get(status);
    if (message != null) {
      return ResponseEntity.status(ex.getStatusCode()).body(new ErrorResponse(message));
    }
    throw ex;
  }
}
