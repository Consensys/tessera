package com.quorum.tessera.data;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadCodec;
import com.quorum.tessera.enclave.PayloadEncoder;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class EncryptedTransactionListener implements Disableable {

  @PreUpdate
  @PrePersist
  public void onSave(EncryptedTransaction encryptedTransaction) {
    if (isDisabled()) {
      return;
    }

    if (encryptedTransaction.getPayload() != null) {
      EncodedPayload encodedPayload = encryptedTransaction.getPayload();
      EncodedPayloadCodec encodedPayloadCodec = encryptedTransaction.getEncodedPayloadCodec();
      byte[] encodedPayloadData =
          PayloadEncoder.create(encodedPayloadCodec).get().encode(encodedPayload);
      encryptedTransaction.setEncodedPayload(encodedPayloadData);
    }
  }

  @PostLoad
  public void onLoad(EncryptedTransaction encryptedTransaction) {
    if (isDisabled()) {
      return;
    }
    EncodedPayloadCodec encodedPayloadCodec = encryptedTransaction.getEncodedPayloadCodec();
    byte[] encodedPayloadData = encryptedTransaction.getEncodedPayload();
    EncodedPayload encodedPayload =
        PayloadEncoder.create(encodedPayloadCodec).get().decode(encodedPayloadData);
    encryptedTransaction.setPayload(encodedPayload);
  }
}
