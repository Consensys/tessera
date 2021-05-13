package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;

public class DecodePayloadHandler implements BatchWorkflowAction {

  private PayloadEncoder encoder;

  public DecodePayloadHandler(PayloadEncoder encoder) {
    this.encoder = encoder;
  }

  @Override
  public boolean execute(BatchWorkflowContext event) {

    EncryptedTransaction encryptedTransaction = event.getEncryptedTransaction();

    EncodedPayload encodedPayload = encoder.decode(encryptedTransaction.getEncodedPayload());

    event.setEncodedPayload(encodedPayload);

    return true;
  }
}
