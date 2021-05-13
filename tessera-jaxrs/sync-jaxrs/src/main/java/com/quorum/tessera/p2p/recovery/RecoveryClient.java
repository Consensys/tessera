package com.quorum.tessera.p2p.recovery;

import com.quorum.tessera.p2p.resend.ResendClient;
import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import java.util.ServiceLoader;

public interface RecoveryClient extends ResendClient {

  boolean pushBatch(String targetUrl, PushBatchRequest request);

  ResendBatchResponse makeBatchResendRequest(String targetUrl, ResendBatchRequest request);

  static RecoveryClient create() {
    return ServiceLoaderUtil.loadSingle(ServiceLoader.load(RecoveryClient.class));
  }
}
