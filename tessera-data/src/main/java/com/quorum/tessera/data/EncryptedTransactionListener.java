package com.quorum.tessera.data;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadCodec;
import com.quorum.tessera.enclave.PayloadEncoder;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptedTransactionListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedTransactionListener.class);

  @PreUpdate
  public void onUpdate(EncryptedTransaction encryptedTransaction) {
    LOGGER.debug("onUpdate {}", encryptedTransaction);

    final EncodedPayload encodedPayload = encryptedTransaction.getPayload();
    final EncodedPayloadCodec encodedPayloadCodec = encryptedTransaction.getEncodedPayloadCodec();
    final PayloadEncoder payloadEncoder = PayloadEncoder.create(encodedPayloadCodec);
    final byte[] encodedPayloadData = payloadEncoder.encode(encodedPayload);
    encryptedTransaction.setEncodedPayload(encodedPayloadData);
  }

  @PrePersist
  public void onSave(EncryptedTransaction encryptedTransaction) {
    LOGGER.debug("onSave {}", encryptedTransaction);

    final EncodedPayload encodedPayload = encryptedTransaction.getPayload();
    final EncodedPayloadCodec encodedPayloadCodec = EncodedPayloadCodec.current();
    final PayloadEncoder payloadEncoder = PayloadEncoder.create(encodedPayloadCodec);
    final byte[] encodedPayloadData = payloadEncoder.encode(encodedPayload);
    encryptedTransaction.setEncodedPayloadCodec(encodedPayloadCodec);
    encryptedTransaction.setEncodedPayload(encodedPayloadData);
  }

  @PostLoad
  public void onLoad(EncryptedTransaction encryptedTransaction) {
    LOGGER.debug("onLoad[{}]", encryptedTransaction);

    final EncodedPayloadCodec encodedPayloadCodec =
        Optional.ofNullable(encryptedTransaction.getEncodedPayloadCodec())
            .orElse(EncodedPayloadCodec.LEGACY);
    final byte[] encodedPayloadData = encryptedTransaction.getEncodedPayload();
    final PayloadEncoder payloadEncoder = PayloadEncoder.create(encodedPayloadCodec);
    final EncodedPayload encodedPayload = payloadEncoder.decode(encodedPayloadData);
    encryptedTransaction.setPayload(encodedPayload);
    encryptedTransaction.setEncodedPayloadCodec(encodedPayloadCodec);
  }
}
