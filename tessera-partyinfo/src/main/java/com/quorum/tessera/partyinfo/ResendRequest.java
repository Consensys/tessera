package com.quorum.tessera.partyinfo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Model representation of a JSON body on incoming HTTP requests.
 * Used when a request is received to resend existing transactions. Contains:
 * <ul>
 *     <li>the public key who is a recipient</li>
 *     <li>the resend type, whether to send a single transaction or all transactions for the given key</li>
 *     <li>the transaction hash to resend in the case the resend type is for an individual transaction</li>
 * </ul>
 */
@ApiModel
public class ResendRequest {

    @ApiModelProperty("Resend type INDIVIDUAL or ALL, to resend a single or all transactions")
    private ResendRequestType type;

    @ApiModelProperty("The recipient public key to resend transactions for")
    private String publicKey;
    
    @ApiModelProperty("Base64 encoded transaction hash")
    private String key;

    public ResendRequestType getType() {
        return type;
    }

    public void setType(final ResendRequestType type) {
        this.type = type;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(final String publicKey) {
        this.publicKey = publicKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

}
