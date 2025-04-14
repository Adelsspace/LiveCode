package ru.hh.blokshnote.utility;

public enum WsPathParam {
  USER("user"),
  ROOM_UUID("roomUuid"),
  MESSAGE_TYPE("message_type");

  private final String label;

  WsPathParam(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
