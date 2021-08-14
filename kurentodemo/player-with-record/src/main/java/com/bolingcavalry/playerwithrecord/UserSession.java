package com.bolingcavalry.playerwithrecord;

import org.kurento.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class UserSession {

  private final Logger log = LoggerFactory.getLogger(UserSession.class);

  private WebRtcEndpoint webRtcEndpoint;
  private MediaPipeline mediaPipeline;
  private PlayerEndpoint playerEndpoint;

  public void setRecorderEndpoint(RecorderEndpoint recorderEndpoint) {
    this.recorderEndpoint = recorderEndpoint;
  }

  private RecorderEndpoint recorderEndpoint;

  public UserSession() {
  }

  public WebRtcEndpoint getWebRtcEndpoint() {
    return webRtcEndpoint;
  }

  public void setWebRtcEndpoint(WebRtcEndpoint webRtcEndpoint) {
    this.webRtcEndpoint = webRtcEndpoint;
  }

  public MediaPipeline getMediaPipeline() {
    return mediaPipeline;
  }

  public void setMediaPipeline(MediaPipeline mediaPipeline) {
    this.mediaPipeline = mediaPipeline;
  }

  public void addCandidate(IceCandidate candidate) {
    webRtcEndpoint.addIceCandidate(candidate);
  }

  public PlayerEndpoint getPlayerEndpoint() {
    return playerEndpoint;
  }

  public void setPlayerEndpoint(PlayerEndpoint playerEndpoint) {
    this.playerEndpoint = playerEndpoint;
  }

  public void release() {
    // 关闭录制组件
    if (recorderEndpoint != null) {
      log.info("do stop recorder endpoint");
      final CountDownLatch stoppedCountDown = new CountDownLatch(1);

      // 增加监听，等待KMS关闭录制组件的结果
      ListenerSubscription subscriptionId = recorderEndpoint
              .addStoppedListener(new EventListener<StoppedEvent>() {
                @Override
                public void onEvent(StoppedEvent event) {
                  log.info("finish stop recorder endpoint");
                  // 关闭成功后，把锁打开，这样stoppedCountDown.await方法就不再阻塞
                  stoppedCountDown.countDown();
                }
              });

      // 执行停止操作
      recorderEndpoint.stop();

      try {
        // 当前线程开始等待
        if (!stoppedCountDown.await(5, TimeUnit.SECONDS)) {
          log.error("Error waiting for recorder to stop");
        }
      } catch (InterruptedException e) {
        log.error("Exception while waiting for state change", e);
      }

      // 移除监听器
      recorderEndpoint.removeStoppedListener(subscriptionId);
    }

    this.playerEndpoint.stop();
    this.mediaPipeline.release();
  }
}
