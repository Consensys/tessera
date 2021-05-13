package com.quorum.tessera.p2p.recovery;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.Test;

public class PushBatchRequestTest {

  @Test
  public void createOnly() {
    PushBatchRequest request = new PushBatchRequest();
    assertThat(request.getEncodedPayloads()).isNull();
  }

  @Test
  public void createPopulated() {
    byte[] encodedPayload = "HELLO".getBytes();

    PushBatchRequest request = new PushBatchRequest(Arrays.asList(encodedPayload));

    assertThat(request.getEncodedPayloads()).containsExactly(encodedPayload);
  }

  @Test
  public void createAndPopulate() {
    byte[] encodedPayload = "HELLO".getBytes();

    PushBatchRequest request = new PushBatchRequest();
    request.setEncodedPayloads(Arrays.asList(encodedPayload));

    assertThat(request.getEncodedPayloads()).containsExactly(encodedPayload);
  }
}
