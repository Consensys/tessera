package com.quorum.tessera.api.model;

import java.util.Optional;

public class ResendResponse {

    private byte[] payload;

    public ResendResponse() {}

    public ResendResponse(byte[] payload) {
        this.payload = payload;
    }

    /** @return Optional encoded payload */
    public Optional<byte[]> getPayload() {
        return Optional.ofNullable(payload);
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
}
