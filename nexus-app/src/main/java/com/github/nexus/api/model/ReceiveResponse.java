package com.github.nexus.api.model;

public class ReceiveResponse {

    private String payload;

    public ReceiveResponse(String payload) {
        this.payload = payload;
    }

    public ReceiveResponse() {
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
