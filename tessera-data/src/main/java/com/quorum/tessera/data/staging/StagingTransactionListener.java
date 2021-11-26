package com.quorum.tessera.data.staging;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadCodec;
import com.quorum.tessera.enclave.PayloadEncoder;
import jakarta.persistence.PostLoad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StagingTransactionListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(StagingTransactionListener.class);

  @PostLoad
  public void onLoad(StagingTransaction stagingTransaction) {
    LOGGER.debug("onLoad[{}]", stagingTransaction);

    final EncodedPayloadCodec encodedPayloadCodec = stagingTransaction.getEncodedPayloadCodec();
    final byte[] encodedPayloadData = stagingTransaction.getPayload();
    final PayloadEncoder payloadEncoder = PayloadEncoder.create(encodedPayloadCodec);
    final EncodedPayload encodedPayload = payloadEncoder.decode(encodedPayloadData);
    stagingTransaction.setEncodedPayload(encodedPayload);
  }
}
