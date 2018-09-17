package com.quorum.tessera.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Model representation of a JSON body on outgoing HTTP requests
 *
 * Contains a Base64 encoded string that is the decrypting payload of a transaction
 */
@ApiModel
public class ReceiveResponse {

    @ApiModelProperty("Encode response servicing receive requests")
    private String payload;

    public ReceiveResponse(final String payload) {
        this.payload = payload;
    }

    public ReceiveResponse() {
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(final String payload) {
        this.payload = payload;
    }
}
