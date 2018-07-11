package com.github.tessera.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;

@ApiModel
public class ReceiveRequest {

    @NotNull
    @ApiModelProperty("Encoded sender public key")
    private String key;

    @NotNull
    @ApiModelProperty("Encoded recipient public key")
    private String to;

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getTo() {
        return to;
    }

    public void setTo(final String to) {
        this.to = to;
    }
}
