package com.quorum.tessera.enclave.rest;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EnclavePayload implements Serializable {

    @XmlMimeType("base64Binary")
    private byte[] data;

    @XmlMimeType("base64Binary")
    private byte[] senderKey;

    @XmlMimeType("base64Binary")
    private List<byte[]> recipientPublicKeys;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getSenderKey() {
        return senderKey;
    }

    public void setSenderKey(byte[] senderKey) {
        this.senderKey = senderKey;
    }

    public List<byte[]> getRecipientPublicKeys() {
        return recipientPublicKeys;
    }

    public void setRecipientPublicKeys(List<byte[]> recipientPublicKeys) {
        this.recipientPublicKeys = recipientPublicKeys;
    }
}
