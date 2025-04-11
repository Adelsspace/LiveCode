package ru.hh.blokshnote.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import ru.hh.blokshnote.controller.RoomController;
import ru.hh.blokshnote.dto.error.ErrorResponse;


@RestControllerAdvice(assignableTypes = RoomController.class)
public class RoomExceptionHandler {

  @ExceptionHandler(ResponseStatusException.class)
  public Object handleResponseStatusException(ResponseStatusException ex) {
    if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
      return handleNotFound(ex);
    }
    if (ex.getStatusCode() == HttpStatus.CONFLICT) {
      return handleConflict(ex);
    }
    if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
      return handleBadRequest(ex);
    }
    throw ex;
  }

  public ResponseEntity<?> handleNotFound(ResponseStatusException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Room does not exist"));
  }

  public ResponseEntity<?> handleConflict(ResponseStatusException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("User with this name already exists"));
  }

  public ResponseEntity<?> handleBadRequest(ResponseStatusException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Invalid admin token"));
  }
}
