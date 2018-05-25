package com.github.nexus.api.model;

public class PrivateKey {

    private PrivateKeyBytes data;
    private String type;

    public PrivateKeyBytes getData() {
        return data;
    }

    public void setData(PrivateKeyBytes data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
