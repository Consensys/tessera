package com.quorum.tessera.p2p.resend;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ResendResponseTest {
  // Not covered in open pojo
  @Test
  public void createWithPayload() {
    ResendResponse resendResponse = new ResendResponse("HELLOW".getBytes());
    assertThat(resendResponse.getPayload()).isPresent();
  }
}
