package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PreparePayloadForRecipient implements BatchWorkflowAction {

  @Override
  public boolean execute(final BatchWorkflowContext event) {
    final EncodedPayload payload = event.getEncodedPayload();
    final PublicKey targetPublicKey = event.getRecipientKey();

    if (!Objects.equals(payload.getSenderKey(), targetPublicKey)) {
      // we are the sender, so need to format the payload for the recipient
      // which is: for PSV, all recipients and one box, or just one box and one recipient
      final EncodedPayload adjustedPayload =
          EncodedPayload.Builder.forRecipient(payload, targetPublicKey).build();
      event.setPayloadsToPublish(Set.of(adjustedPayload));
      return true;
    }

    // the resend key is the sender of the tx, trying to rebuild its contents

    // we have the keys, so just matching keys to boxes
    if (!payload.getRecipientKeys().isEmpty()) {
      final int numberOfBoxes = payload.getRecipientBoxes().size();
      // we know the recipients, we just need to format them per recipient we have
      // but only for ones we have boxes for
      final Set<EncodedPayload> formattedPayloads =
          payload.getRecipientKeys().stream()
              .filter(key -> payload.getRecipientKeys().indexOf(key) < numberOfBoxes)
              .map(key -> EncodedPayload.Builder.forRecipient(payload, key).build())
              .collect(Collectors.toSet());
      event.setPayloadsToPublish(formattedPayloads);
      return true;
    }

    // We only have boxes, no recipients (pre-1.0 standard private)
    // Create individual payloads with each box and search for each box's key.
    final Set<EncodedPayload> formattedPayloads =
        payload.getRecipientBoxes().stream()
            .map(
                box ->
                    EncodedPayload.Builder.from(payload)
                        .withRecipientBoxes(List.of(box.getData()))
                        .build())
            .collect(Collectors.toSet());
    event.setPayloadsToPublish(formattedPayloads);
    return true;
  }
}
