package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PrivacyMode;
import java.util.Objects;

public class StandardPrivateOnlyFilter implements BatchWorkflowFilter {

  @Override
  public boolean filter(final BatchWorkflowContext context) {
    final EncodedPayload payload = context.getEncodedPayload();

    return Objects.nonNull(payload)
        && Objects.equals(payload.getPrivacyMode(), PrivacyMode.STANDARD_PRIVATE);
  }
}
