package ru.hh.blokshnote.dto.websocket;

public class PositionDto {
  private int lineNumber;
  private int column;

  public int getLineNumber() {
    return lineNumber;
  }

  public void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  public int getColumn() {
    return column;
  }

  public void setColumn(int column) {
    this.column = column;
  }
}
