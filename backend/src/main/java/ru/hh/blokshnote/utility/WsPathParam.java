package ru.hh.blokshnote.utility;

public enum WsPathParam {
  USER("user"),
  ROOM_UUID("roomUuid"),
  ;

  private final String label;

  WsPathParam(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
