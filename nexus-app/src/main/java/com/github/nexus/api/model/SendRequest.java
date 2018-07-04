package com.github.nexus.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;

@ApiModel
public class SendRequest {

    @NotNull
    @ApiModelProperty("Encyrpted payload to send to other parties.")
    private String payload;

    @ApiModelProperty("Sender public key")
    private String from;

    @ApiModelProperty("Recipient public keys")
    private String[] to;

    public String getPayload() {
        return payload;
    }

    public void setPayload(final String payload) {
        this.payload = payload;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(final String from) {
        this.from = from;
    }

    public String[] getTo() {
        if (to == null) {
            return new String[]{};
        }
        return to.clone();
    }

    public void setTo(final String[] to) {
        this.to = to;
    }
}
