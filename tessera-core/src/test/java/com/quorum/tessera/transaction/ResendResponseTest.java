package com.quorum.tessera.transaction;

import com.quorum.tessera.enclave.EncodedPayload;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResendResponseTest {

    @Test
    public void buildWithPayload() {
        final EncodedPayload payload = EncodedPayload.Builder.create().build();

        final ResendResponse response = ResendResponse.Builder.create().withPayload(payload).build();

        assertThat(response).isNotNull();
        assertThat(response.getPayload()).isSameAs(payload);
    }

    @Test
    public void buildWithoutPayload() {
        final ResendResponse response = ResendResponse.Builder.create().build();

        assertThat(response).isNotNull();
        assertThat(response.getPayload()).isNull();
    }
}
