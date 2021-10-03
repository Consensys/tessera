package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.enclave.EncodedPayload;

public class DecodePayloadHandler implements BatchWorkflowAction {

  @Override
  public boolean execute(BatchWorkflowContext event) {

    EncryptedTransaction encryptedTransaction = event.getEncryptedTransaction();

    EncodedPayload encodedPayload = encryptedTransaction.getPayload();

    event.setEncodedPayload(encodedPayload);

    return true;
  }
}
