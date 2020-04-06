package com.quorum.tessera.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class ReceiveResponseTest {

    @Test
    public void createInstanceWithPayload() {
        byte[] payload = "HELLOW".getBytes();
        ReceiveResponse instance = new ReceiveResponse(payload, 0, null, null);

        assertThat(instance.getPayload()).isEqualTo(payload);
    }
}
