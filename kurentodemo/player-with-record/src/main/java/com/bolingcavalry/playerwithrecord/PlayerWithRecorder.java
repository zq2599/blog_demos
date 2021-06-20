package com.bolingcavalry.playerwithrecord;

import org.kurento.client.KurentoClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: 启动类
 * @date 2021/6/19 09:42
 */
@EnableWebSocket
@SpringBootApplication
public class PlayerWithRecorder implements WebSocketConfigurer {

  @Bean
  public PlayerHandler handler() {
    return new PlayerHandler();
  }

  @Bean
  public PlayerRecorderHandler playerRecorderHandler() {
    return new PlayerRecorderHandler();
  }

  /**
   * 实例化KurentoClient，入参是KMS地址
   * @return
   */
  @Bean
  public KurentoClient kurentoClient() {
    return KurentoClient.create("ws://192.168.91.128:8888/kurento");
  }

  @Bean
  public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
    ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
    container.setMaxTextMessageBufferSize(32768);
    return container;
  }

  /**
   * 标准的WebSocket处理类绑定
   * @param registry
   */
  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(handler(), "/player");
//    registry.addHandler(playerRecorderHandler(), "/player");
  }

  public static void main(String[] args) throws Exception {
    SpringApplication.run(PlayerWithRecorder.class, args);
  }
}
