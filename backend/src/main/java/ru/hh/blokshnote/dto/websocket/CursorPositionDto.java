package ru.hh.blokshnote.dto.websocket;

public class CursorPositionDto {
    private Position position;
    private String username;

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}