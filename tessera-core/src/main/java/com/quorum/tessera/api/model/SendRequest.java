package com.quorum.tessera.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import javax.validation.constraints.Size;

/**
 * Model representation of a JSON body on incoming HTTP requests
 *
 * Used when a new transaction is to be created where this node is the sender
 */
@ApiModel
public class SendRequest {

    @Size(min = 1)
    @NotNull
    @ApiModelProperty("Encrypted payload to send to other parties.")
    private String payload;

    @ApiModelProperty("Sender public key")
    private String from;

    @ApiModelProperty("Recipient public keys")
    private String[] to;

    public String getPayload() {
        return this.payload;
    }

    public void setPayload(final String payload) {
        this.payload = payload;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(final String from) {
        this.from = from;
    }

    public String[] getTo() {
        if (this.to == null) {
            return new String[]{};
        }

        return Arrays.copyOf(this.to, this.to.length);
    }

    public void setTo(final String[] to) {
        this.to = to;
    }
}
