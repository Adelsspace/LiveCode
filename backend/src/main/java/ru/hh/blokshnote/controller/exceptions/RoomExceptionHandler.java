package ru.hh.blokshnote.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.hh.blokshnote.controller.RoomController;
import ru.hh.blokshnote.dto.room.error.RoomError;
import ru.hh.blokshnote.dto.user.error.UserError;

@RestControllerAdvice(assignableTypes = RoomController.class)
public class RoomExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(IllegalArgumentException.class)
    public RoomError handleIllegalArgumentException(IllegalArgumentException ex) {
        return new RoomError("Room does not exist");
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(IllegalStateException.class)
    public UserError handleIllegalStateException(IllegalStateException ex) {
        return new UserError("User with this name already exists");
    }
}
