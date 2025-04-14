package ru.hh.blokshnote.utility;

public enum WebSocketPathParam {
  USER("user"),
  ROOM_UUID("roomUuid");

  private final String label;

  WebSocketPathParam(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
