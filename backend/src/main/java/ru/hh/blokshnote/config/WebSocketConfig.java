package ru.hh.blokshnote.config;

import com.corundumstudio.socketio.AuthorizationResult;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import ru.hh.blokshnote.handler.RoomSocketHandler;
import ru.hh.blokshnote.utility.WsPathParam;

@org.springframework.context.annotation.Configuration
public class WebSocketConfig {

  public static final String ROOM_URI_TEMPLATE = "/ws/room/connect";

  @Value("${socketio.host}")
  private String host;
  @Value("${socketio.port}")
  private int port;

  @Bean(destroyMethod = "stop")
  public SocketIOServer socketIOServer(RoomSocketHandler handler) {
    Configuration config = new Configuration();
    config.setHostname(host);
    config.setPort(port);
    config.setAuthorizationListener(data -> {
          if (data.getSingleUrlParam(WsPathParam.USER.getLabel()) != null
              && data.getSingleUrlParam(WsPathParam.ROOM_UUID.getLabel()) != null
          ) {
            return AuthorizationResult.SUCCESSFUL_AUTHORIZATION;
          }
          return AuthorizationResult.FAILED_AUTHORIZATION;
        }
    );
    SocketIOServer server = new SocketIOServer(config);
    handler.registerListeners(server.addNamespace(ROOM_URI_TEMPLATE));
    server.start();
    return server;
  }
}

