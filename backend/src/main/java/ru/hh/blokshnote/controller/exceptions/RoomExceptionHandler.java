package ru.hh.blokshnote.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    } else if (ex.getStatusCode() == HttpStatus.CONFLICT) {
      return handleConflict(ex);
    }
    throw ex;
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse handleNotFound(ResponseStatusException ex) {
    return new ErrorResponse("Room does not exist");
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  public ErrorResponse handleConflict(ResponseStatusException ex) {
    return new ErrorResponse("User with this name already exists");
  }
}
