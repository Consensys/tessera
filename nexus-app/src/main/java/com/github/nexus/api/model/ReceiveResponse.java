package com.github.nexus.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class ReceiveResponse {

    @ApiModelProperty("Encode response servicing recieve requests")
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
