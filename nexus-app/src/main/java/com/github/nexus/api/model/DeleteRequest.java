package com.github.nexus.api.model;

import io.swagger.annotations.ApiModel;
import javax.validation.constraints.NotNull;

@ApiModel
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
