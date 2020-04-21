package com.quorum.tessera.partyinfo;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
