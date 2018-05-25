package com.github.nexus.api.model;

import javax.validation.constraints.NotNull;

public class DeleteRequest {

    @NotNull
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
