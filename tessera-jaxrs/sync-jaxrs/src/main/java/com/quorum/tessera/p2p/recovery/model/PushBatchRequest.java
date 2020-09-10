package com.quorum.tessera.p2p.recovery.model;

import io.swagger.annotations.ApiModel;

import javax.xml.bind.annotation.XmlMimeType;
import java.util.List;

/** Model representation of a list of encoded payloads */
@ApiModel
public class PushBatchRequest {

    @XmlMimeType("base64Binary")
    private List<byte[]> encodedPayloads;

    public PushBatchRequest() {}

    public PushBatchRequest(List<byte[]> encodedPayloads) {
        this.encodedPayloads = encodedPayloads;
    }

    public List<byte[]> getEncodedPayloads() {
        return encodedPayloads;
    }

    public void setEncodedPayloads(List<byte[]> encodedPayloads) {
        this.encodedPayloads = encodedPayloads;
    }
}
