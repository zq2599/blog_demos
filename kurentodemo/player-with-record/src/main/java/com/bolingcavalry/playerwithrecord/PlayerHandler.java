package com.bolingcavalry.playerwithrecord;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

import org.kurento.client.EndOfStreamEvent;
import org.kurento.client.ErrorEvent;
import org.kurento.client.EventListener;
import org.kurento.client.IceCandidate;
import org.kurento.client.IceCandidateFoundEvent;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.MediaState;
import org.kurento.client.MediaStateChangedEvent;
import org.kurento.client.PlayerEndpoint;
import org.kurento.client.ServerManager;
import org.kurento.client.VideoInfo;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.commons.exception.KurentoException;
import org.kurento.jsonrpc.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * @author will (zq2599@gmail.com)
 * @version 1.0
 * @description: 业务信令处理类
 * @date 2021/6/19 09:44
 */
public class PlayerHandler extends TextWebSocketHandler {

  @Autowired
  private KurentoClient kurento;

  private final Logger log = LoggerFactory.getLogger(PlayerHandler.class);
  private final Gson gson = new GsonBuilder().create();
  private final ConcurrentHashMap<String, UserSession> users = new ConcurrentHashMap<>();

  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
    String sessionId = session.getId();
    log.debug("用户[{}]收到websocket命令: {} from sessionId", sessionId, jsonMessage);

    try {
      switch (jsonMessage.get("id").getAsString()) {
        // 开始播放
        case "start":
          start(session, jsonMessage);
          break;
        // 停止播放
        case "stop":
          stop(sessionId);
          break;
        // 暂停
        case "pause":
          pause(sessionId);
          break;
        // 恢复
        case "resume":
          resume(session);
          break;
        // 生成监控内容
        case "debugDot":
          debugDot(session);
          break;
        // 前进或者倒退
        case "doSeek":
          doSeek(session, jsonMessage);
          break;
        // 取位置
        case "getPosition":
          getPosition(session);
          break;
        // 更新WebRTC的ICE数据
        case "onIceCandidate":
          onIceCandidate(sessionId, jsonMessage);
          break;
        default:
          sendError(session, "Invalid message with id " + jsonMessage.get("id").getAsString());
          break;
      }
    } catch (Throwable t) {
      log.error("Exception handling message {} in sessionId {}", jsonMessage, sessionId, t);
      sendError(session, t.getMessage());
    }
  }

  private void start(final WebSocketSession session, JsonObject jsonMessage) {
    // 1.新建MediaPipeline对象
    MediaPipeline pipeline = kurento.createMediaPipeline();

    // 2. 新建连接浏览器的WebRtcEndpoint对象
    WebRtcEndpoint webRtcEndpoint = new WebRtcEndpoint.Builder(pipeline).build();

    // 3.1 取出要播放的地址
    String videourl = jsonMessage.get("videourl").getAsString();

    // 3.2 新建负责播放的PlayerEndpoint对象
    final PlayerEndpoint playerEndpoint = new PlayerEndpoint.Builder(pipeline, videourl).build();

    // 4 playerEndpoint连接webRtcEndpoint，这样playerEndpoint解码出的内容通过webRtcEndpoint给到浏览器
    playerEndpoint.connect(webRtcEndpoint);

    // 5. WebRtc相关的操作
    // 5.1 一旦收到KMS的candidate就立即给到前端
    webRtcEndpoint.addIceCandidateFoundListener(new EventListener<IceCandidateFoundEvent>() {

      @Override
      public void onEvent(IceCandidateFoundEvent event) {
        JsonObject response = new JsonObject();
        response.addProperty("id", "iceCandidate");
        response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
        try {
          synchronized (session) {
            session.sendMessage(new TextMessage(response.toString()));
          }
        } catch (IOException e) {
          log.debug(e.getMessage());
        }
      }
    });

    // SDP offer是前端给的
    String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
    // 给前端准备SDP answer
    String sdpAnswer = webRtcEndpoint.processOffer(sdpOffer);

    log.info("[Handler::start] SDP Offer from browser to KMS:\n{}", sdpOffer);
    log.info("[Handler::start] SDP Answer from KMS to browser:\n{}", sdpAnswer);

    JsonObject response = new JsonObject();
    response.addProperty("id", "startResponse");
    response.addProperty("sdpAnswer", sdpAnswer);
    sendMessage(session, response.toString());

    // 6. 和媒体播放有关的操作
    // 6.1 KMS会发送和媒体播放有关的消息过来，如果连接媒体成功，就把获取到的相关参数给到前端
    webRtcEndpoint.addMediaStateChangedListener(new EventListener<MediaStateChangedEvent>() {
      @Override
      public void onEvent(MediaStateChangedEvent event) {

        if (event.getNewState() == MediaState.CONNECTED) {
          // 媒体相关的信息可以用getVideoInfo去的
          VideoInfo videoInfo = playerEndpoint.getVideoInfo();

          JsonObject response = new JsonObject();
          response.addProperty("id", "videoInfo");
          response.addProperty("isSeekable", videoInfo.getIsSeekable());
          response.addProperty("initSeekable", videoInfo.getSeekableInit());
          response.addProperty("endSeekable", videoInfo.getSeekableEnd());
          response.addProperty("videoDuration", videoInfo.getDuration());

          // 把这些媒体信息给前端
          sendMessage(session, response.toString());
        }
      }
    });

    // 让KMS把它的ICD Candidate发过来(前面的监听会收到)
    webRtcEndpoint.gatherCandidates();

    // 7.1 添加媒体播放的监听：异常消息
    playerEndpoint.addErrorListener(new EventListener<ErrorEvent>() {
      @Override
      public void onEvent(ErrorEvent event) {
        log.info("ErrorEvent: {}", event.getDescription());
        // 通知前端停止播放
        sendPlayEnd(session);
      }
    });

    // 7.2 添加媒体播放的监听：播放结束
    playerEndpoint.addEndOfStreamListener(new EventListener<EndOfStreamEvent>() {
      @Override
      public void onEvent(EndOfStreamEvent event) {
        log.info("EndOfStreamEvent: {}", event.getTimestamp());
        // 通知前端停止播放
        sendPlayEnd(session);
      }
    });

    // 通过KMS开始连接远程媒体
    playerEndpoint.play();

    // 将pipeline、webRtcEndpoint、playerEndpoint这些信息放入UserSession对象中，
    // 这样方便处理前端发过来的各种命令
    final UserSession user = new UserSession();
    user.setMediaPipeline(pipeline);
    user.setWebRtcEndpoint(webRtcEndpoint);
    user.setPlayerEndpoint(playerEndpoint);
    users.put(session.getId(), user);
  }

  /**
   * 暂停播放
   * @param sessionId
   */
  private void pause(String sessionId) {
    UserSession user = users.get(sessionId);

    if (user != null) {
      user.getPlayerEndpoint().pause();
    }
  }

  /**
   * 从暂停恢复
   * @param session
   */
  private void resume(final WebSocketSession session) {
    UserSession user = users.get(session.getId());

    if (user != null) {
      user.getPlayerEndpoint().play();
      VideoInfo videoInfo = user.getPlayerEndpoint().getVideoInfo();

      JsonObject response = new JsonObject();
      response.addProperty("id", "videoInfo");
      response.addProperty("isSeekable", videoInfo.getIsSeekable());
      response.addProperty("initSeekable", videoInfo.getSeekableInit());
      response.addProperty("endSeekable", videoInfo.getSeekableEnd());
      response.addProperty("videoDuration", videoInfo.getDuration());
      sendMessage(session, response.toString());
    }
  }

  /**
   * 停止播放
   * @param sessionId
   */
  private void stop(String sessionId) {
    UserSession user = users.remove(sessionId);

    if (user != null) {
      user.release();
    }
  }

  /**
   * 取得Gstreamer的dot内容，这样的内容可以被graphviz工具解析成拓扑图
   * @param session
   */
  private void debugDot(final WebSocketSession session) {
    UserSession user = users.get(session.getId());

    if (user != null) {
      final String pipelineDot = user.getMediaPipeline().getGstreamerDot();
      try (PrintWriter out = new PrintWriter("player.dot")) {
        out.println(pipelineDot);
      } catch (IOException ex) {
        log.error("[Handler::debugDot] Exception: {}", ex.getMessage());
      }
      final String playerDot = user.getPlayerEndpoint().getElementGstreamerDot();
      try (PrintWriter out = new PrintWriter("player-decoder.dot")) {
        out.println(playerDot);
      } catch (IOException ex) {
        log.error("[Handler::debugDot] Exception: {}", ex.getMessage());
      }
    }

    ServerManager sm = kurento.getServerManager();
    log.warn("[Handler::debugDot] CPU COUNT: {}", sm.getCpuCount());
    log.warn("[Handler::debugDot] CPU USAGE: {}", sm.getUsedCpu(1000));
    log.warn("[Handler::debugDot] RAM USAGE: {}", sm.getUsedMemory());
  }

  /**
   * 跳转到指定位置
   * @param session
   * @param jsonMessage
   */
  private void doSeek(final WebSocketSession session, JsonObject jsonMessage) {
    UserSession user = users.get(session.getId());

    if (user != null) {
      try {
        user.getPlayerEndpoint().setPosition(jsonMessage.get("position").getAsLong());
      } catch (KurentoException e) {
        log.debug("The seek cannot be performed");
        JsonObject response = new JsonObject();
        response.addProperty("id", "seek");
        response.addProperty("message", "Seek failed");
        sendMessage(session, response.toString());
      }
    }
  }

  /**
   * 取得当前播放位置
   * @param session
   */
  private void getPosition(final WebSocketSession session) {
    UserSession user = users.get(session.getId());

    if (user != null) {
      long position = user.getPlayerEndpoint().getPosition();

      JsonObject response = new JsonObject();
      response.addProperty("id", "position");
      response.addProperty("position", position);
      sendMessage(session, response.toString());
    }
  }

  /**
   * 收到前端的Ice candidate后，立即发给KMS
   * @param sessionId
   * @param jsonMessage
   */
  private void onIceCandidate(String sessionId, JsonObject jsonMessage) {
    UserSession user = users.get(sessionId);

    if (user != null) {
      JsonObject jsonCandidate = jsonMessage.get("candidate").getAsJsonObject();
      IceCandidate candidate =
          new IceCandidate(jsonCandidate.get("candidate").getAsString(), jsonCandidate
              .get("sdpMid").getAsString(), jsonCandidate.get("sdpMLineIndex").getAsInt());
      user.getWebRtcEndpoint().addIceCandidate(candidate);
    }
  }

  /**
   * 通知前端停止播放
   * @param session
   */
  public void sendPlayEnd(WebSocketSession session) {
    if (users.containsKey(session.getId())) {
      JsonObject response = new JsonObject();
      response.addProperty("id", "playEnd");
      sendMessage(session, response.toString());
    }
  }

  /**
   * 将错误信息发给前端
   * @param session
   * @param message
   */
  private void sendError(WebSocketSession session, String message) {
    if (users.containsKey(session.getId())) {
      JsonObject response = new JsonObject();
      response.addProperty("id", "error");
      response.addProperty("message", message);
      sendMessage(session, response.toString());
    }
  }

  /**
   * 给前端发送消息的方法用synchronized修饰，
   * 因为收到KMS通知的时机不确定，此时可能正在给前端发送消息，存在同时调用sendMessage的可能
   * @param session
   * @param message
   */
  private synchronized void sendMessage(WebSocketSession session, String message) {
    try {
      session.sendMessage(new TextMessage(message));
    } catch (IOException e) {
      log.error("Exception sending message", e);
    }
  }

  /**
   * 和前端的websocket连接断开后，此方法会被调用
   * @param session
   * @param status
   * @throws Exception
   */
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    stop(session.getId());
  }
}
