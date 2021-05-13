package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import com.quorum.tessera.transaction.publish.PublishPayloadException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleEncodedPayloadPublisher implements BatchWorkflowAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(SingleEncodedPayloadPublisher.class);

  private final PayloadPublisher payloadPublisher;

  public SingleEncodedPayloadPublisher(final PayloadPublisher payloadPublisher) {
    this.payloadPublisher = Objects.requireNonNull(payloadPublisher);
  }

  @Override
  public boolean execute(final BatchWorkflowContext event) {
    final PublicKey recipientKey = event.getRecipientKey();

    return event.getPayloadsToPublish().stream()
        .allMatch(
            payload -> {
              try {
                payloadPublisher.publishPayload(payload, recipientKey);
                return true;
              } catch (final PublishPayloadException ex) {
                LOGGER.warn(
                    "Unable to publish payload to recipient {} during resend",
                    recipientKey.encodeToBase64());
                return false;
              }
            });
  }
}
