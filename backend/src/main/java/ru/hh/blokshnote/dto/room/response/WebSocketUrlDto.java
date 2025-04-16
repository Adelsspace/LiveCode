package ru.hh.blokshnote.dto.room.response;

public class WebSocketUrlDto {

  private String wsConnectUrl;

  public WebSocketUrlDto(String wsConnectUrl) {
    this.wsConnectUrl = wsConnectUrl;
  }

  public WebSocketUrlDto() {
  }

  public String getWsConnectUrl() {
    return wsConnectUrl;
  }

  public void setWsConnectUrl(String wsConnectUrl) {
    this.wsConnectUrl = wsConnectUrl;
  }
}
