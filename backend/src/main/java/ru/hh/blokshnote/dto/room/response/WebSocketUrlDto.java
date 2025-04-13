package ru.hh.blokshnote.dto.room.response;

public class WebSocketUrlDto {

  public WebSocketUrlDto(String wsConnectUrl) {
    this.wsConnectUrl = wsConnectUrl;
  }

  public WebSocketUrlDto() {
  }

  private String wsConnectUrl;

  public String getWsConnectUrl() {
    return wsConnectUrl;
  }

  public void setWsConnectUrl(String wsConnectUrl) {
    this.wsConnectUrl = wsConnectUrl;
  }
}
