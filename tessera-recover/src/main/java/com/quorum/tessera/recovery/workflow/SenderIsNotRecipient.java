package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SenderIsNotRecipient implements BatchWorkflowFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(SenderIsNotRecipient.class);

  private final Enclave enclave;

  public SenderIsNotRecipient(final Enclave enclave) {
    this.enclave = Objects.requireNonNull(enclave);
  }

  @Override
  public boolean filter(final BatchWorkflowContext context) {
    final PublicKey recipientKey = context.getRecipientKey();

    final boolean valid = !enclave.getPublicKeys().contains(recipientKey);

    if (!valid) {
      // we are trying to send something to ourselves - don't do it
      LOGGER.debug("Resend request for own key {}, not continuing", recipientKey.encodeToBase64());
    }
    return valid;
  }
}
