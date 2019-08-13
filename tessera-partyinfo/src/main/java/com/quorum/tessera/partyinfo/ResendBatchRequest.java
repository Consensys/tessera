package com.quorum.tessera.partyinfo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Model representation of a JSON body on incoming HTTP requests.
 * Used when a request is received to resend existing transactions. Contains:
 * <ul>
 *     <li>the public key who is a recipient</li>
 *     <li>the batch size</li>
 * </ul>
 */
@ApiModel
public class ResendBatchRequest {

    @ApiModelProperty("The recipient public key to resend transactions for")
    private String publicKey;
    
    @ApiModelProperty("batch size")
    private int batchSize;

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(final String publicKey) {
        this.publicKey = publicKey;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
