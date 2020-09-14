package com.quorum.tessera.recovery.resend;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResendBatchResponseTest {

    @Test
    public void create() {
        ResendBatchResponse response = ResendBatchResponse.from(100);
        assertThat(response).isNotNull();
        assertThat(response.getTotal()).isEqualTo(100);
    }
}
