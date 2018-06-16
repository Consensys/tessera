package com.github.nexus.api.model;

public class ResendRequest {

    private ResendRequestType type;
    private String publicKey;
    private String key;

    public ResendRequestType getType() {
        return type;
    }

    public void setType(ResendRequestType type) {
        this.type = type;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
