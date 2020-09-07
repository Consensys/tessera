package com.quorum.tessera.p2p;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResendResponseTest {

    @Test
    public void defaultInstanceHasNoPayload() {
        ResendResponse resendResponse = new ResendResponse();
        assertThat(resendResponse.getPayload()).isNotPresent();
    }

    @Test
    public void setPayload() {
        ResendResponse resendResponse = new ResendResponse();
        resendResponse.setPayload("HELLOW".getBytes());
        assertThat(resendResponse.getPayload()).isPresent();

    }

    @Test
    public void createWithPayload() {
        ResendResponse resendResponse = new ResendResponse("HELLOW".getBytes());
        assertThat(resendResponse.getPayload()).isPresent();
    }

}
