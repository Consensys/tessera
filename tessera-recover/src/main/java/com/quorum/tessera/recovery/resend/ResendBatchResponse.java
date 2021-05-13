package com.quorum.tessera.recovery.resend;

public interface ResendBatchResponse {

  long getTotal();

  static ResendBatchResponse from(long total) {
    return () -> total;
  }
}
