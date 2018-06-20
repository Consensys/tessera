package com.github.nexus.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class ResendRequest {

    @ApiModelProperty("Resend type INDIVIDUAL or ALL")
    private ResendRequestType type;

    @ApiModelProperty("TODO: Define this publicKey, what is it?")
    private String publicKey;
    
    @ApiModelProperty("TODO: Define this key, what is it?")
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
