package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.nacl.Nonce;

import java.util.*;

public class EncodedPayloadBuilder {

    private PublicKey senderKey;

    private byte[] cipherText;

    private byte[] cipherTextNonce;

    private final List<byte[]> recipientBoxes = new ArrayList<>();

    private byte[] recipientNonce;

    private final List<PublicKey> recipientKeys = new ArrayList<>();

    private int privacyFlag;

    private final Map<TxHash, byte[]> affectedContractTransactions = new HashMap<>();

    private byte[] execHash;

    public static EncodedPayloadBuilder create() {
        return new EncodedPayloadBuilder();
    }

    private EncodedPayloadBuilder() {}

    public EncodedPayloadBuilder withSenderKey(final PublicKey senderKey) {
        this.senderKey = senderKey;
        return this;
    }

    public EncodedPayloadBuilder withCipherText(final byte[] cipherText) {
        this.cipherText = cipherText;
        return this;
    }

    public EncodedPayloadBuilder withRecipientKeys(final PublicKey... recipientKeys) {
        this.recipientKeys.addAll(Arrays.asList(recipientKeys));
        return this;
    }

    public EncodedPayloadBuilder withCipherTextNonce(final byte[] cipherTextNonce) {
        this.cipherTextNonce = cipherTextNonce;
        return this;
    }

    public EncodedPayloadBuilder withRecipientNonce(final byte[] recipientNonce) {
        this.recipientNonce = recipientNonce;
        return this;
    }

    public EncodedPayloadBuilder withRecipientBoxes(final List<byte[]> recipientBoxes) {
        this.recipientBoxes.addAll(recipientBoxes);
        return this;
    }

    public EncodedPayloadBuilder withPrivacyFlag(final int privacyFlag) {
        this.privacyFlag = privacyFlag;
        return this;
    }

    public EncodedPayloadBuilder withAffectedContractTransactions(
            final Map<TxHash, byte[]> affectedContractTransactions) {
        this.affectedContractTransactions.putAll(affectedContractTransactions);
        return this;
    }

    public EncodedPayloadBuilder withExecHash(final byte[] execHash) {
        this.execHash = execHash;
        return this;
    }

    public EncodedPayload build() {
        return new EncodedPayload(
                senderKey,
                cipherText,
                new Nonce(cipherTextNonce),
                recipientBoxes,
                new Nonce(recipientNonce),
                recipientKeys,
                PrivacyMode.fromFlag(privacyFlag),
                affectedContractTransactions,
                execHash);
    }
}
