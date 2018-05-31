package com.github.nexus.api.model;

public class SendResponse {

    private String key;

    public SendResponse(String key) {
        this.key = key;
    }

    public SendResponse() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
