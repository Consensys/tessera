package com.quorum.tessera.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.xml.bind.annotation.XmlMimeType;
/**
 * Model representation of a JSON body on outgoing HTTP requests
 *
 * <p>Contains a Base64 encoded string that is the decrypting payload of a transaction
 */
@ApiModel
public class ReceiveResponse {

    @XmlMimeType("base64Binary")
    @ApiModelProperty("Encode response servicing receive requests")
    private byte[] payload;

    public ReceiveResponse(final byte[] payload) {
        this.payload = payload;
    }

    public ReceiveResponse() {}

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(final byte[] payload) {
        this.payload = payload;
    }
}
