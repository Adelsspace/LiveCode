package ru.hh.blokshnote.dto.websocket;

public class ChangeDto {
  private RangeDto range;
  private String text;
  private boolean forceMoveMarkers;
  private int version;

  public RangeDto getRange() {
    return range;
  }

  public void setRange(RangeDto range) {
    this.range = range;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public boolean isForceMoveMarkers() {
    return forceMoveMarkers;
  }

  public void setForceMoveMarkers(boolean forceMoveMarkers) {
    this.forceMoveMarkers = forceMoveMarkers;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }
}
