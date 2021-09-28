package com.quorum.tessera.data;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadCodec;
import com.quorum.tessera.enclave.PayloadEncoder;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptedTransactionListener implements Disableable {

  private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedTransactionListener.class);

  @PreUpdate
  @PrePersist
  public void onSave(EncryptedTransaction encryptedTransaction) {
    if (isDisabled()) {
      return;
    }
    if (encryptedTransaction.getPayload() != null) {
      EncodedPayload encodedPayload = encryptedTransaction.getPayload();

      final EncodedPayloadCodec encodedPayloadCodec;
      if (encodedPayload.getEncodedPayloadCodec() != null) {
        encodedPayloadCodec = encodedPayload.getEncodedPayloadCodec();
      } else if (encryptedTransaction.getEncodedPayloadCodec() != null) {
        encodedPayloadCodec = encryptedTransaction.getEncodedPayloadCodec();
      } else {
        throw new IllegalStateException("No codec defined");
      }

      PayloadEncoder payloadEncoder = lookup(encodedPayloadCodec);
      byte[] encodedPayloadData = payloadEncoder.encode(encodedPayload);
      encryptedTransaction.setEncodedPayload(encodedPayloadData);
    }
  }

  @PostLoad
  public void onLoad(EncryptedTransaction encryptedTransaction) {
    LOGGER.debug("onLoad[{}]", encryptedTransaction);
    if (isDisabled()) {
      return;
    }
    EncodedPayloadCodec encodedPayloadCodec = encryptedTransaction.getEncodedPayloadCodec();
    byte[] encodedPayloadData = encryptedTransaction.getEncodedPayload();
    PayloadEncoder payloadEncoder = lookup(encodedPayloadCodec);
    EncodedPayload encodedPayload = payloadEncoder.decode(encodedPayloadData);
    encryptedTransaction.setPayload(encodedPayload);
  }

  private static PayloadEncoder lookup(EncodedPayloadCodec encodedPayloadCodec) {
    return PayloadEncoder.create(encodedPayloadCodec)
        .orElseThrow(
            () -> new IllegalStateException("No encoder found for " + encodedPayloadCodec));
  }
}
