package com.quorum.tessera.api;

import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;

import javax.xml.bind.annotation.XmlMimeType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PayloadEncryptResponse {

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

    public PayloadEncryptResponse(final byte[] senderKey,
                                  final byte[] cipherText,
                                  final byte[] cipherTextNonce,
                                  final List<byte[]> recipientBoxes,
                                  final byte[] recipientNonce,
                                  final List<byte[]> recipientKeys,
                                  final int privacyMode,
                                  final Map<byte[], byte[]> affectedContractTransactions,
                                  final byte[] execHash) {
        this.senderKey = senderKey;
        this.cipherText = cipherText;
        this.cipherTextNonce = cipherTextNonce;
        this.recipientBoxes = recipientBoxes;
        this.recipientNonce = recipientNonce;
        this.recipientKeys = recipientKeys;
        this.privacyMode = privacyMode;
        this.affectedContractTransactions = affectedContractTransactions;
        this.execHash = execHash;
    }

    public byte[] getSenderKey() {
        return senderKey;
    }

    public byte[] getCipherText() {
        return cipherText;
    }

    public byte[] getCipherTextNonce() {
        return cipherTextNonce;
    }

    public List<byte[]> getRecipientBoxes() {
        return recipientBoxes;
    }

    public byte[] getRecipientNonce() {
        return recipientNonce;
    }

    public List<byte[]> getRecipientKeys() {
        return recipientKeys;
    }

    public int getPrivacyMode() {
        return privacyMode;
    }

    public Map<byte[], byte[]> getAffectedContractTransactions() {
        return affectedContractTransactions;
    }

    public byte[] getExecHash() {
        return execHash;
    }

    public static class Builder {

        private Builder() {
        }

        public static Builder create() {
            return new PayloadEncryptResponse.Builder();
        }

        public static Builder from(final EncodedPayload encodedPayload) {
            final Map<byte[], byte[]> affectedContractTransactionMap =
                encodedPayload.getAffectedContractTransactions().entrySet()
                    .stream()
                    .collect(Collectors.toMap(e -> e.getKey().getBytes(), e -> e.getValue().getData()));

            return create()
                .withPrivacyMode(encodedPayload.getPrivacyMode())
                .withSenderKey(encodedPayload.getSenderKey())
                .withRecipientNonce(encodedPayload.getRecipientNonce())
                .withRecipientKeys(encodedPayload.getRecipientKeys())
                .withRecipientBoxes(
                    encodedPayload.getRecipientBoxes().stream()
                        .map(RecipientBox::getData).collect(Collectors.toList()))
                .withPrivacyMode(encodedPayload.getPrivacyMode())
                .withExecHash(encodedPayload.getExecHash())
                .withCipherText(encodedPayload.getCipherText())
                .withCipherTextNonce(encodedPayload.getCipherTextNonce())
                .withAffectedContractTransactions(affectedContractTransactionMap);
        }

        private PublicKey senderKey;

        private byte[] cipherText;

        private Nonce cipherTextNonce;

        private Nonce recipientNonce;

        private List<byte[]> recipientBoxes = new ArrayList<>();

        private List<PublicKey> recipientKeys = new ArrayList<>();

        private PrivacyMode privacyMode = PrivacyMode.STANDARD_PRIVATE;

        private Map<byte[], byte[]> affectedContractTransactions = Collections.emptyMap();

        private byte[] execHash = new byte[0];

        public Builder withSenderKey(final PublicKey senderKey) {
            this.senderKey = senderKey;
            return this;
        }

        public Builder withCipherText(final byte[] cipherText) {
            this.cipherText = cipherText;
            return this;
        }

        public Builder withRecipientKeys(final List<PublicKey> recipientKeys) {
            this.recipientKeys = recipientKeys;
            return this;
        }

        public Builder withCipherTextNonce(final Nonce cipherTextNonce) {
            this.cipherTextNonce = cipherTextNonce;
            return this;
        }

        public Builder withRecipientNonce(final Nonce recipientNonce) {
            this.recipientNonce = recipientNonce;
            return this;
        }

        public Builder withRecipientBoxes(final List<byte[]> recipientBoxes) {
            this.recipientBoxes = recipientBoxes;
            return this;
        }

        public Builder withPrivacyMode(final PrivacyMode privacyMode) {
            this.privacyMode = privacyMode;
            return this;
        }

        public Builder withAffectedContractTransactions(final Map<byte[], byte[]> affectedContractTransactions) {
            this.affectedContractTransactions = affectedContractTransactions;
            return this;
        }

        public Builder withExecHash(final byte[] execHash) {
            this.execHash = execHash;
            return this;
        }

        public PayloadEncryptResponse build() {
            return new PayloadEncryptResponse(
                senderKey.getKeyBytes(),
                cipherText,
                cipherTextNonce.getNonceBytes(),
                recipientBoxes,
                recipientNonce.getNonceBytes(),
                recipientKeys.stream().map(PublicKey::getKeyBytes).collect(Collectors.toList()),
                privacyMode.getPrivacyFlag(),
                affectedContractTransactions,
                execHash
            );
        }
    }

}
