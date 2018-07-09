package com.github.tessera.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class SendResponse {

    @ApiModelProperty("TODO: Define this key as something")
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
