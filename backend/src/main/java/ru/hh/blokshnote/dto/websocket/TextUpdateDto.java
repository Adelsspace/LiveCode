package ru.hh.blokshnote.dto.websocket;

import java.util.List;

public class TextUpdateDto {
    private List<Change> changes;
    private String username;

    public List<Change> getChanges() {
        return changes;
    }

    public void setChanges(List<Change> changes) {
        this.changes = changes;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}