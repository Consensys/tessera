package com.quorum.tessera.p2p.recovery;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ResendBatchResponseTest {

  @Test
  public void testCreate() {
    ResendBatchResponse resendBatchResponse = new ResendBatchResponse(1);
    assertThat(resendBatchResponse.getTotal()).isEqualTo(1);
    resendBatchResponse = new ResendBatchResponse();
    resendBatchResponse.setTotal(2);
    assertThat(resendBatchResponse.getTotal()).isEqualTo(2);
  }
}
