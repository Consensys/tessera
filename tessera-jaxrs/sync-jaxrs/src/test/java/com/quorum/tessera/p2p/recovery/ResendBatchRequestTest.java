package com.quorum.tessera.p2p.recovery;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResendBatchRequestTest {

    @Test
    public void testCreate() {
        ResendBatchRequest resendBatchRequest = new ResendBatchRequest();
        resendBatchRequest.setBatchSize(1);
        resendBatchRequest.setPublicKey("key");
        assertThat(resendBatchRequest.getBatchSize()).isEqualTo(1);
        assertThat(resendBatchRequest.getPublicKey()).isEqualTo("key");
    }
}
