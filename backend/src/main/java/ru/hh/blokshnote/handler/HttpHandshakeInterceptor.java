package ru.hh.blokshnote.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpHandshakeInterceptor implements HandshakeInterceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpHandshakeInterceptor.class);

  @Override
  public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                 Map<String, Object> attributes) {

    Map<String, String> parameters = parseQuery(request.getURI().getQuery());
    String roomId = parameters.get("roomId");
    String token = parameters.get("token");
    if (roomId != null && token != null) return true;
    LOGGER.info("roomId or token is null");
    return false;
  }

  @Override
  public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                             Exception exception) {
  }

  private Map<String, String> parseQuery(String query) {
    if (query == null) return Collections.emptyMap();

    return Arrays.stream(query.split("&"))
        .map(p -> p.split("=", 2))
        .filter(p -> p.length == 2)
        .collect(Collectors.toMap(p -> p[0], p -> p[1]));
  }
}
