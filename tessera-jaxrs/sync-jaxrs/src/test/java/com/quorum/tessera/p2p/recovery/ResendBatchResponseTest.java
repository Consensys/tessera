package com.quorum.tessera.p2p.recovery;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResendBatchResponseTest {

    @Test
    public void testCreate() {
        ResendBatchResponse resendBatchResponse = new ResendBatchResponse(1912L);
        assertThat(resendBatchResponse.getTotal()).isEqualTo(1912L);
    }
}
