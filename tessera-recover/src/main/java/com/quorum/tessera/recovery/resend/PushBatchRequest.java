package com.quorum.tessera.recovery.resend;

import com.quorum.tessera.enclave.EncodedPayloadCodec;
import java.util.List;

public interface PushBatchRequest {

  List<byte[]> getEncodedPayloads();

  EncodedPayloadCodec getEncodedPayloadCodec();

  static PushBatchRequest from(
      List<byte[]> encodedPayloads, EncodedPayloadCodec encodedPayloadCodec) {
    return new PushBatchRequest() {
      @Override
      public List<byte[]> getEncodedPayloads() {
        return List.copyOf(encodedPayloads);
      }

      @Override
      public EncodedPayloadCodec getEncodedPayloadCodec() {
        return encodedPayloadCodec;
      }
    };
  }
}
