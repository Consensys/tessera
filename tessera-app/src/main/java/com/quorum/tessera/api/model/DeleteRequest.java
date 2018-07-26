package com.quorum.tessera.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;

/**
 * Model representation of a JSON body on incoming HTTP requests
 *
 * Contrains information that is used to delete a transaction
 */
@ApiModel
public class DeleteRequest {

    @NotNull
    @ApiModelProperty(name = "Encoded transaction hash")
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

}
