package com.quorum.tessera.recovery.resend;

import java.util.List;

public interface PushBatchRequest {

  List<byte[]> getEncodedPayloads();

  static PushBatchRequest from(List<byte[]> encodedPayloads) {
    return () -> encodedPayloads;
  }
}
