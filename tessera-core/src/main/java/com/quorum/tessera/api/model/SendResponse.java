package com.quorum.tessera.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Model representation of a JSON body on incoming HTTP requests
 *
 * A response to a {@link SendRequest} after the transaction has been
 * distributed and saved
 */
@ApiModel
public class SendResponse {

    @ApiModelProperty("Base64 encoded transaction hash")
    private String key;

    public SendResponse(final String key) {
        this.key = key;
    }

    public SendResponse() {
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

}
