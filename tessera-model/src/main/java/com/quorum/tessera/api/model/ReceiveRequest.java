package com.quorum.tessera.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;

/**
 * Model representation of a JSON body on incoming HTTP requests
 *
 * Contains information for retrieving a decrypting payload
 * such as the message hash and the public key to decrypt with
 *
 */
@ApiModel
public class ReceiveRequest {

    @NotNull
    @ApiModelProperty("Encoded transaction hash")
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
