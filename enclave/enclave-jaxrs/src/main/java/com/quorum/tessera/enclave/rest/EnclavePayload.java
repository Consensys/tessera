package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.enclave.PrivacyMode;

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

    private PrivacyMode privacyMode;

    private List<KeyValuePair> affectedContractTransactions;

    @XmlMimeType("base64Binary")
    private byte[] execHash;

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

    public PrivacyMode getPrivacyMode() {
        return privacyMode;
    }

    public void setPrivacyMode(PrivacyMode privacyMode) {
        this.privacyMode = privacyMode;
    }

    public List<KeyValuePair> getAffectedContractTransactions() {
        return affectedContractTransactions;
    }

    public void setAffectedContractTransactions(List<KeyValuePair> affectedContractTransactions) {
        this.affectedContractTransactions = affectedContractTransactions;
    }

    public byte[] getExecHash() {
        return execHash;
    }

    public void setExecHash(byte[] execHash) {
        this.execHash = execHash;
    }
}
