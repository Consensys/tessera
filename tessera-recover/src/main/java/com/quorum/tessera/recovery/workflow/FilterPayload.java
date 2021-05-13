package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Objects;

public class FilterPayload implements BatchWorkflowFilter {

  private Enclave enclave;

  public FilterPayload(Enclave enclave) {
    this.enclave = enclave;
  }

  @Override
  public boolean filter(BatchWorkflowContext context) {

    EncodedPayload encodedPayload = context.getEncodedPayload();
    PublicKey recipientPublicKey = context.getRecipientKey();

    final boolean isCurrentNodeSender =
        encodedPayload.getRecipientKeys().contains(recipientPublicKey)
            && enclave.getPublicKeys().contains(encodedPayload.getSenderKey());

    final boolean isRequestedNodeSender =
        Objects.equals(encodedPayload.getSenderKey(), recipientPublicKey);

    return isCurrentNodeSender || isRequestedNodeSender;
  }
}
