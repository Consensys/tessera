package com.quorum.tessera.p2p.resend;

import java.util.Optional;

public class ResendResponse {

  private byte[] payload;

  public ResendResponse() {}

  public ResendResponse(byte[] payload) {
    this.payload = payload;
  }

  /** @return Optional encoded payload */
  public Optional<byte[]> getPayload() {
    return Optional.ofNullable(payload);
  }

  public void setPayload(byte[] payload) {
    this.payload = payload;
  }
}
