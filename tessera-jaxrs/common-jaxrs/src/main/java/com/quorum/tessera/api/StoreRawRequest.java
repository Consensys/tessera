package com.quorum.tessera.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlInlineBinaryData;
import java.util.Optional;

/**
 * Model representation of a JSON body on incoming HTTP requests
 *
 * Used when a new raw transaction is to be created where this node is the sender
 */
@ApiModel
public class StoreRawRequest {

    @Size(min = 1)
    @NotNull
    @XmlInlineBinaryData
    private byte[] payload;

    @ApiModelProperty("Sender public key")
    @XmlInlineBinaryData
    private byte[] from;

    @XmlInlineBinaryData
    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public Optional<byte[]> getFrom() {
        return Optional.ofNullable(from);
    }

    public void setFrom(byte[] from) {
        this.from = from;
    }
}
