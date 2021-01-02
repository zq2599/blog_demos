/*
 * Copyright 2018 Kurento (https://www.kurento.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bolingcavalry.helloworld;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.kurento.client.*;
import org.kurento.jsonrpc.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Kurento Java Tutorial - WebSocket message handler.
 */
public class Handler extends TextWebSocketHandler
{
  private static final Logger log = LoggerFactory.getLogger(Handler.class);
  private static final Gson gson = new GsonBuilder().create();

  private final ConcurrentHashMap<String, UserSession> users =
      new ConcurrentHashMap<>();

  @Autowired
  private KurentoClient kurento;

  /**
   * Invoked after WebSocket negotiation has succeeded and the WebSocket connection is
   * opened and ready for use.
   */
  @Override
  public void afterConnectionEstablished(WebSocketSession session)
      throws Exception
  {
    log.info("[Handler::afterConnectionEstablished] New WebSocket connection, sessionId: {}",
        session.getId());
  }

  /**
   * Invoked after the WebSocket connection has been closed by either side, or after a
   * transport error has occurred. Although the session may technically still be open,
   * depending on the underlying implementation, sending messages at this point is
   * discouraged and most likely will not succeed.
   */
  @Override
  public void afterConnectionClosed(final WebSocketSession session,
      CloseStatus status) throws Exception
  {
    if (!status.equalsCode(CloseStatus.NORMAL)) {
      log.warn("[Handler::afterConnectionClosed] status: {}, sessionId: {}",
          status, session.getId());
    }

    stop(session);
  }

  /**
   * Invoked when a new WebSocket message arrives.
   */
  @Override
  protected void handleTextMessage(WebSocketSession session,
      TextMessage message) throws Exception
  {
    final String sessionId = session.getId();
    JsonObject jsonMessage = gson.fromJson(message.getPayload(),
        JsonObject.class);

    log.info("[Handler::handleTextMessage] message: {}, sessionId: {}",
        jsonMessage, sessionId);

    try {
      final String messageId = jsonMessage.get("id").getAsString();
      switch (messageId) {
        case "PROCESS_SDP_OFFER":
          // Start: Create user session and process SDP Offer
          handleProcessSdpOffer(session, jsonMessage);
          break;
        case "ADD_ICE_CANDIDATE":
          handleAddIceCandidate(session, jsonMessage);
          break;
        case "STOP":
          handleStop(session, jsonMessage);
          break;
        case "ERROR":
          handleError(session, jsonMessage);
          break;
        default:
          // Ignore the message
          log.warn("[Handler::handleTextMessage] Skip, invalid message, id: {}",
              messageId);
          break;
      }
    } catch (Throwable ex) {
      log.error("[Handler::handleTextMessage] Exception: {}, sessionId: {}",
          ex, sessionId);
      sendError(session, "[Kurento] Exception: " + ex.getMessage());
    }
  }

  /**
   * Handle an error from the underlying WebSocket message transport.
   */
  @Override
  public void handleTransportError(WebSocketSession session,
      Throwable exception) throws Exception
  {
    log.error("[Handler::handleTransportError] Exception: {}, sessionId: {}",
        exception, session.getId());

    session.close(CloseStatus.SERVER_ERROR);
  }

  private synchronized void sendMessage(final WebSocketSession session,
      String message)
  {
    log.debug("[Handler::sendMessage] {}", message);

    if (!session.isOpen()) {
      log.warn("[Handler::sendMessage] Skip, WebSocket session isn't open");
      return;
    }

    final String sessionId = session.getId();
    if (!users.containsKey(sessionId)) {
      log.warn("[Handler::sendMessage] Skip, unknown user, id: {}",
          sessionId);
      return;
    }

    try {
      session.sendMessage(new TextMessage(message));
    } catch (IOException ex) {
      log.error("[Handler::sendMessage] Exception: {}", ex.getMessage());
    }
  }

  private void sendError(final WebSocketSession session, String errMsg)
  {
    log.error(errMsg);

    if (users.containsKey(session.getId())) {
      JsonObject message = new JsonObject();
      message.addProperty("id", "ERROR");
      message.addProperty("message", errMsg);
      sendMessage(session, message.toString());
    }
  }

  // PROCESS_SDP_OFFER ---------------------------------------------------------

  private void initBaseEventListeners(final WebSocketSession session,
      BaseRtpEndpoint baseRtpEp, final String className)
  {
    log.info("[Handler::initBaseEventListeners] name: {}, class: {}, sessionId: {}",
        baseRtpEp.getName(), className, session.getId());

    // Event: Some error happened
    baseRtpEp.addErrorListener(new EventListener<ErrorEvent>() {
      @Override
      public void onEvent(ErrorEvent ev) {
        log.error("[{}::ErrorEvent] Error code {}: '{}', source: {}, timestamp: {}, tags: {}, description: {}",
            className, ev.getErrorCode(), ev.getType(), ev.getSource().getName(),
            ev.getTimestamp(), ev.getTags(), ev.getDescription());

        sendError(session, "[Kurento] " + ev.getDescription());
        stop(session);
      }
    });

    // Event: Media is flowing into this sink
    baseRtpEp.addMediaFlowInStateChangeListener(
        new EventListener<MediaFlowInStateChangeEvent>() {
      @Override
      public void onEvent(MediaFlowInStateChangeEvent ev) {
        log.info("[{}::{}] source: {}, timestamp: {}, tags: {}, state: {}, padName: {}, mediaType: {}",
            className, ev.getType(), ev.getSource().getName(), ev.getTimestamp(),
            ev.getTags(), ev.getState(), ev.getPadName(), ev.getMediaType());
      }
    });

    // Event: Media is flowing out of this source
    baseRtpEp.addMediaFlowOutStateChangeListener(
        new EventListener<MediaFlowOutStateChangeEvent>() {
      @Override
      public void onEvent(MediaFlowOutStateChangeEvent ev) {
        log.info("[{}::{}] source: {}, timestamp: {}, tags: {}, state: {}, padName: {}, mediaType: {}",
            className, ev.getType(), ev.getSource().getName(), ev.getTimestamp(),
            ev.getTags(), ev.getState(), ev.getPadName(), ev.getMediaType());
      }
    });

    // Event: [TODO write meaning of this event]
    baseRtpEp.addConnectionStateChangedListener(
        new EventListener<ConnectionStateChangedEvent>() {
      @Override
      public void onEvent(ConnectionStateChangedEvent ev) {
        log.info("[{}::{}] source: {}, timestamp: {}, tags: {}, oldState: {}, newState: {}",
            className, ev.getType(), ev.getSource().getName(), ev.getTimestamp(),
            ev.getTags(), ev.getOldState(), ev.getNewState());
      }
    });

    // Event: [TODO write meaning of this event]
    baseRtpEp.addMediaStateChangedListener(
        new EventListener<MediaStateChangedEvent>() {
      @Override
      public void onEvent(MediaStateChangedEvent ev) {
        log.info("[{}::{}] source: {}, timestamp: {}, tags: {}, oldState: {}, newState: {}",
            className, ev.getType(), ev.getSource().getName(), ev.getTimestamp(),
            ev.getTags(), ev.getOldState(), ev.getNewState());
      }
    });

    // Event: This element will (or will not) perform media transcoding
    baseRtpEp.addMediaTranscodingStateChangeListener(
        new EventListener<MediaTranscodingStateChangeEvent>() {
      @Override
      public void onEvent(MediaTranscodingStateChangeEvent ev) {
        log.info("[{}::{}] source: {}, timestamp: {}, tags: {}, state: {}, binName: {}, mediaType: {}",
            className, ev.getType(), ev.getSource().getName(), ev.getTimestamp(),
            ev.getTags(), ev.getState(), ev.getBinName(), ev.getMediaType());
      }
    });
  }

  private void initWebRtcEventListeners(final WebSocketSession session,
      final WebRtcEndpoint webRtcEp)
  {
    log.info("[Handler::initWebRtcEventListeners] name: {}, sessionId: {}",
        webRtcEp.getName(), session.getId());

    // Event: The ICE backend found a local candidate during Trickle ICE
    webRtcEp.addIceCandidateFoundListener(
        new EventListener<IceCandidateFoundEvent>() {
      @Override
      public void onEvent(IceCandidateFoundEvent ev) {
        log.debug("[WebRtcEndpoint::{}] source: {}, timestamp: {}, tags: {}, candidate: {}",
            ev.getType(), ev.getSource().getName(), ev.getTimestamp(),
            ev.getTags(), JsonUtils.toJson(ev.getCandidate()));

        JsonObject message = new JsonObject();
        message.addProperty("id", "ADD_ICE_CANDIDATE");
        message.add("candidate", JsonUtils.toJsonObject(ev.getCandidate()));
        sendMessage(session, message.toString());
      }
    });

    // Event: The ICE backend changed state
    webRtcEp.addIceComponentStateChangeListener(
        new EventListener<IceComponentStateChangeEvent>() {
      @Override
      public void onEvent(IceComponentStateChangeEvent ev) {
        log.debug("[WebRtcEndpoint::{}] source: {}, timestamp: {}, tags: {}, streamId: {}, componentId: {}, state: {}",
            ev.getType(), ev.getSource().getName(), ev.getTimestamp(),
            ev.getTags(), ev.getStreamId(), ev.getComponentId(), ev.getState());
      }
    });

    // Event: The ICE backend finished gathering ICE candidates
    webRtcEp.addIceGatheringDoneListener(
        new EventListener<IceGatheringDoneEvent>() {
      @Override
      public void onEvent(IceGatheringDoneEvent ev) {
        log.info("[WebRtcEndpoint::{}] source: {}, timestamp: {}, tags: {}",
            ev.getType(), ev.getSource().getName(), ev.getTimestamp(),
            ev.getTags());
      }
    });

    // Event: The ICE backend selected a new pair of ICE candidates for use
    webRtcEp.addNewCandidatePairSelectedListener(
        new EventListener<NewCandidatePairSelectedEvent>() {
      @Override
      public void onEvent(NewCandidatePairSelectedEvent ev) {
        log.info("[WebRtcEndpoint::{}] name: {}, timestamp: {}, tags: {}, streamId: {}, local: {}, remote: {}",
            ev.getType(), ev.getSource().getName(), ev.getTimestamp(),
            ev.getTags(), ev.getCandidatePair().getStreamID(),
            ev.getCandidatePair().getLocalCandidate(),
            ev.getCandidatePair().getRemoteCandidate());
      }
    });
  }

  private void initWebRtcEndpoint(final WebSocketSession session,
      final WebRtcEndpoint webRtcEp, String sdpOffer)
  {
    initBaseEventListeners(session, webRtcEp, "WebRtcEndpoint");
    initWebRtcEventListeners(session, webRtcEp);

    final String sessionId = session.getId();
    final String name = "user" + sessionId + "_webrtcendpoint";
    webRtcEp.setName(name);

    /*
    OPTIONAL: Force usage of an Application-specific STUN server.
    Usually this is configured globally in KMS WebRTC settings file:
    /etc/kurento/modules/kurento/WebRtcEndpoint.conf.ini

    But it can also be configured per-application, as shown:

    log.info("[Handler::initWebRtcEndpoint] Using STUN server: 193.147.51.12:3478");
    webRtcEp.setStunServerAddress("193.147.51.12");
    webRtcEp.setStunServerPort(3478);
    */

    // Continue the SDP Negotiation: Generate an SDP Answer
    final String sdpAnswer = webRtcEp.processOffer(sdpOffer);

    log.info("[Handler::initWebRtcEndpoint] name: {}, SDP Offer from browser to KMS:\n{}",
        name, sdpOffer);
    log.info("[Handler::initWebRtcEndpoint] name: {}, SDP Answer from KMS to browser:\n{}",
        name, sdpAnswer);

    JsonObject message = new JsonObject();
    message.addProperty("id", "PROCESS_SDP_ANSWER");
    message.addProperty("sdpAnswer", sdpAnswer);
    sendMessage(session, message.toString());
  }

  private void startWebRtcEndpoint(WebRtcEndpoint webRtcEp)
  {
    // Calling gatherCandidates() is when the Endpoint actually starts working.
    // In this tutorial, this is emphasized for demonstration purposes by
    // launching the ICE candidate gathering in its own method.
    webRtcEp.gatherCandidates();
  }

  /**
   * 检查当前行中是否是指定的多个编码器中的一个
   * @param rtpMapLine
   * @param filters
   * @return
   */
  private static boolean isFilterCodec(String rtpMapLine, String[] filters) {

    for(int i=0; i<filters.length; i++) {
      if(rtpMapLine.indexOf(filters[i])>0) {
        return true;
      }
    }

    return false;
  }


  private static String codecFilter(String rawSDP, String[] filters) {
    if(null==filters || filters.length<1) {
      log.info("skip codecFilter");
      return rawSDP;
    }

    log.info("start codecFilter, filter codec types : {}", filters);

    String[] array = rawSDP.split("\r\n");
    List<String> list = new ArrayList<String>();

    boolean isSkip = false;

    // 逐行检查
    for(int i=0;i<array.length;i++) {
      String line = array[i];

      // 如果当前行以"a=rtpmap"开始，就是编码器配置信息
      if(line.startsWith("a=rtpmap")) {

        // 检查是不是要过滤的编码器其中的一种
        if(isFilterCodec(line, filters)) {
          isSkip = true;
        } else {
          isSkip = false;
          list.add(line);
        }

      } else {
        // isSkip等于true，表示当前行是被过滤的编码器配置的一部分，要跳过
        if(isSkip) {
          continue;
        }

        list.add(line);
      }
    }

    return String.join("\r\n", list);
  }

  private void handleProcessSdpOffer(final WebSocketSession session,
      JsonObject jsonMessage)
  {
    // ---- Session handling

    final String sessionId = session.getId();

    log.info("[Handler::handleStart] User count: {}", users.size());
    log.info("[Handler::handleStart] New user, id: {}", sessionId);

    final UserSession user = new UserSession();
    users.put(sessionId, user);


    // ---- Media pipeline

    log.info("[Handler::handleStart] Create Media Pipeline");

    final MediaPipeline pipeline = kurento.createMediaPipeline();
    user.setMediaPipeline(pipeline);

    final WebRtcEndpoint webRtcEp =
        new WebRtcEndpoint.Builder(pipeline).build();
    user.setWebRtcEndpoint(webRtcEp);
    webRtcEp.connect(webRtcEp);


    // ---- Endpoint configuration

    String sdpOffer = jsonMessage.get("sdpOffer").getAsString();




//    sdpOffer = codecFilter(sdpOffer,new String[]{"VP8", "H264"});







    initWebRtcEndpoint(session, webRtcEp, sdpOffer);

    log.info("[Handler::handleStart] New WebRtcEndpoint: {}",
        webRtcEp.getName());


    // ---- Endpoint startup

    startWebRtcEndpoint(webRtcEp);


    // ---- Debug
    // final String pipelineDot = pipeline.getGstreamerDot();
    // try (PrintWriter out = new PrintWriter("pipeline.dot")) {
    //   out.println(pipelineDot);
    // } catch (IOException ex) {
    //   log.error("[Handler::start] Exception: {}", ex.getMessage());
    // }
  }

  // ADD_ICE_CANDIDATE ---------------------------------------------------------

  private void handleAddIceCandidate(final WebSocketSession session,
      JsonObject jsonMessage)
  {
    final String sessionId = session.getId();
    if (!users.containsKey(sessionId)) {
      log.warn("[Handler::handleAddIceCandidate] Skip, unknown user, id: {}",
          sessionId);
      return;
    }

    final UserSession user = users.get(sessionId);
    final JsonObject jsonCandidate =
        jsonMessage.get("candidate").getAsJsonObject();
    final IceCandidate candidate =
        new IceCandidate(jsonCandidate.get("candidate").getAsString(),
        jsonCandidate.get("sdpMid").getAsString(),
        jsonCandidate.get("sdpMLineIndex").getAsInt());

    WebRtcEndpoint webRtcEp = user.getWebRtcEndpoint();
    webRtcEp.addIceCandidate(candidate);
  }

  // STOP ----------------------------------------------------------------------

  private void stop(final WebSocketSession session)
  {
    // Remove the user session and release all resources
    final UserSession user = users.remove(session.getId());
    if (user != null) {
      MediaPipeline mediaPipeline = user.getMediaPipeline();
      if (mediaPipeline != null) {
        log.info("[Handler::stop] Release the Media Pipeline");
        mediaPipeline.release();
      }
    }
  }

  private void handleStop(final WebSocketSession session,
      JsonObject jsonMessage)
  {
    stop(session);
  }

  // ERROR ---------------------------------------------------------------------

  private void handleError(final WebSocketSession session,
      JsonObject jsonMessage)
  {
    final String errMsg = jsonMessage.get("message").getAsString();
    log.error("Browser error: " + errMsg);

    log.info("Assume that the other side stops after an error...");
    stop(session);
  }

  // ---------------------------------------------------------------------------
}
