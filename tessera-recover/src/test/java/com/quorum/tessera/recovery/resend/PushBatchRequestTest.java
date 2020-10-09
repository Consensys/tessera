package com.quorum.tessera.recovery.resend;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PushBatchRequestTest {

    @Test
    public void create() {
        List<byte[]> payloads = List.of("payload".getBytes(), "another".getBytes());
        PushBatchRequest request = PushBatchRequest.from(payloads);

        assertThat(request).isNotNull();
        assertThat(request.getEncodedPayloads()).containsExactly("payload".getBytes(), "another".getBytes());
    }
}
