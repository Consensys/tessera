package com.quorum.tessera.p2p.recovery;

import com.quorum.tessera.enclave.EncodedPayloadCodec;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.recovery.resend.ResendBatchPublisher;

public class ResendBatchPublisherProvider {

  public static ResendBatchPublisher provider() {

    RecoveryClient client = RecoveryClient.create();
    PayloadEncoder payloadEncoder = PayloadEncoder.create(EncodedPayloadCodec.LEGACY);

    return new RestResendBatchPublisher(payloadEncoder, client);
  }
}
