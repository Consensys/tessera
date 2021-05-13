package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.recovery.resend.ResendRequest;
import com.quorum.tessera.recovery.resend.ResendResponse;
import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import java.util.ServiceLoader;

public interface LegacyResendManager {

  ResendResponse resend(ResendRequest request);

  static LegacyResendManager create() {
    return ServiceLoaderUtil.loadSingle(ServiceLoader.load(LegacyResendManager.class));
  }
}
