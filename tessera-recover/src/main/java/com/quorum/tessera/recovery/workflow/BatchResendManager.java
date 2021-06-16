package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.recovery.resend.PushBatchRequest;
import com.quorum.tessera.recovery.resend.ResendBatchRequest;
import com.quorum.tessera.recovery.resend.ResendBatchResponse;
import java.util.ServiceLoader;

public interface BatchResendManager {

  ResendBatchResponse resendBatch(ResendBatchRequest request);

  void storeResendBatch(PushBatchRequest resendPushBatchRequest);

  static BatchResendManager create() {
    return ServiceLoader.load(BatchResendManager.class).findFirst().get();
  }
}
