package com.github.nexus.api.model;

import javax.validation.constraints.NotNull;

public class ReceiveRequest {

    @NotNull
    private String key;
    @NotNull
    private String to;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
