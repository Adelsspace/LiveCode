package ru.hh.blokshnote.dto.room.response;

import java.util.Set;

public class RoomStateDto {
  private String editorText;
  private Set<String> users;

  public String getEditorText() {
    return editorText;
  }

  public void setEditorText(String editorText) {
    this.editorText = editorText;
  }

  public Set<String> getUsers() {
    return users;
  }

  public void setUsers(Set<String> users) {
    this.users = users;
  }
}
