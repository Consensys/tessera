package com.quorum.tessera.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlMimeType;

/**
 * Model representation of a JSON body on incoming HTTP requests
 *
 * <p>Used when a new transaction is to be created where this node is the sender
 */
@ApiModel
public class SendSignedRequest {

    @XmlMimeType("base64Binary")
    @Size(min = 1)
    @NotNull
    @ApiModelProperty("The hash of the stored payload to send to other parties.")
    private byte[] hash;

    @ApiModelProperty("Recipient public keys")
    private String[] to;

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public String[] getTo() {
        return to;
    }

    public void setTo(final String... to) {
        this.to = to;
    }
}
