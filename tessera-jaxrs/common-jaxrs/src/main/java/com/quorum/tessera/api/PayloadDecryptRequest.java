package com.quorum.tessera.api;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.enclave.TxHash;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;

import javax.xml.bind.annotation.XmlMimeType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PayloadDecryptRequest {

    @XmlMimeType("base64Binary")
    private byte[] senderKey;

    @XmlMimeType("base64Binary")
    private byte[] cipherText;

    @XmlMimeType("base64Binary")
    private byte[] cipherTextNonce;

    @XmlMimeType("base64Binary")
    private List<byte[]> recipientBoxes;

    @XmlMimeType("base64Binary")
    private byte[] recipientNonce;

    @XmlMimeType("base64Binary")
    private List<byte[]> recipientKeys;

    private int privacyMode;

    @XmlMimeType("base64Binary")
    private Map<byte[], byte[]> affectedContractTransactions;

    @XmlMimeType("base64Binary")
    private byte[] execHash;

    public byte[] getSenderKey() {
        return senderKey;
    }

    public void setSenderKey(final byte[] senderKey) {
        this.senderKey = senderKey;
    }

    public byte[] getCipherText() {
        return cipherText;
    }

    public void setCipherText(final byte[] cipherText) {
        this.cipherText = cipherText;
    }

    public byte[] getCipherTextNonce() {
        return cipherTextNonce;
    }

    public void setCipherTextNonce(final byte[] cipherTextNonce) {
        this.cipherTextNonce = cipherTextNonce;
    }

    public List<byte[]> getRecipientBoxes() {
        return recipientBoxes;
    }

    public void setRecipientBoxes(final List<byte[]> recipientBoxes) {
        this.recipientBoxes = recipientBoxes;
    }

    public byte[] getRecipientNonce() {
        return recipientNonce;
    }

    public void setRecipientNonce(final byte[] recipientNonce) {
        this.recipientNonce = recipientNonce;
    }

    public List<byte[]> getRecipientKeys() {
        return recipientKeys;
    }

    public void setRecipientKeys(final List<byte[]> recipientKeys) {
        this.recipientKeys = recipientKeys;
    }

    public int getPrivacyMode() {
        return privacyMode;
    }

    public void setPrivacyMode(final int privacyMode) {
        this.privacyMode = privacyMode;
    }

    public Map<byte[], byte[]> getAffectedContractTransactions() {
        return affectedContractTransactions;
    }

    public void setAffectedContractTransactions(Map<byte[], byte[]> affectedContractTransactions) {
        this.affectedContractTransactions = affectedContractTransactions;
    }

    public byte[] getExecHash() {
        return execHash;
    }

    public void setExecHash(byte[] execHash) {
        this.execHash = execHash;
    }

    public EncodedPayload toEncodedPayload() {
        final Map<TxHash, byte[]> affectedTxns = affectedContractTransactions.entrySet()
            .stream()
            .collect(Collectors.toMap(e -> TxHash.from(e.getKey()), Map.Entry::getValue));

        return EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from(senderKey))
            .withCipherText(cipherText)
            .withCipherTextNonce(cipherTextNonce)
            .withRecipientBoxes(recipientBoxes)
            .withRecipientNonce(recipientNonce)
            .withRecipientKeys(recipientKeys.stream().map(PublicKey::from).collect(Collectors.toList()))
            .withPrivacyFlag(privacyMode)
            .withAffectedContractTransactions(affectedTxns)
            .withExecHash(execHash)
            .build();
    }

}
