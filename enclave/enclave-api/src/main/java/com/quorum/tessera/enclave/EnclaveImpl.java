package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quorum.tessera.encryption.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.KeyManager;
import com.quorum.tessera.encryption.MasterKey;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.encryption.SharedKey;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class EnclaveImpl implements Enclave {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveImpl.class);

    private final Encryptor encryptor;

    private final KeyManager keyManager;

    public EnclaveImpl(Encryptor encryptor, KeyManager keyManager) {
        this.encryptor = Objects.requireNonNull(encryptor);
        this.keyManager = Objects.requireNonNull(keyManager);
    }

    @Override
    public EncodedPayload encryptPayload(
            final byte[] message, final PublicKey senderPublicKey, final List<PublicKey> recipientPublicKeys) {
        final MasterKey masterKey = encryptor.createMasterKey();
        final Nonce nonce = encryptor.randomNonce();
        final Nonce recipientNonce = encryptor.randomNonce();

        final byte[] cipherText = encryptor.sealAfterPrecomputation(message, nonce, masterKey);

        final List<byte[]> encryptedMasterKeys =
                buildRecipientMasterKeys(senderPublicKey, recipientPublicKeys, recipientNonce, masterKey);

        return EncodedPayload.Builder.create()
                .withSenderKey(senderPublicKey)
                .withCipherText(cipherText)
                .withCipherTextNonce(nonce)
                .withRecipientBoxes(encryptedMasterKeys)
                .withRecipientNonce(recipientNonce)
                .withRecipientKeys(recipientPublicKeys)
                .build();
    }

    @Override
    public byte[] createNewRecipientBox(final EncodedPayload payload, final PublicKey publicKey) {

        if (payload.getRecipientKeys().isEmpty() || payload.getRecipientBoxes().isEmpty()) {
            throw new RuntimeException("No key or recipient-box to use");
        }

        final MasterKey master =
                this.getMasterKey(
                        payload.getRecipientKeys().get(0), payload.getSenderKey(),
                        payload.getRecipientNonce(), payload.getRecipientBoxes().get(0).getData());

        final List<byte[]> sealedMasterKeyList =
                this.buildRecipientMasterKeys(
                        payload.getSenderKey(), List.of(publicKey), payload.getRecipientNonce(), master);

        return sealedMasterKeyList.get(0);
    }

    @Override
    public EncodedPayload encryptPayload(
            final RawTransaction rawTransaction, final List<PublicKey> recipientPublicKeys) {
        final MasterKey masterKey =
                this.getMasterKey(
                        rawTransaction.getFrom(), rawTransaction.getFrom(),
                        rawTransaction.getNonce(), rawTransaction.getEncryptedKey());

        final Nonce recipientNonce = encryptor.randomNonce();
        final List<byte[]> encryptedMasterKeys =
                buildRecipientMasterKeys(rawTransaction.getFrom(), recipientPublicKeys, recipientNonce, masterKey);

        return EncodedPayload.Builder.create()
                .withSenderKey(rawTransaction.getFrom())
                .withCipherText(rawTransaction.getEncryptedPayload())
                .withCipherTextNonce(rawTransaction.getNonce())
                .withRecipientBoxes(encryptedMasterKeys)
                .withRecipientNonce(recipientNonce)
                .withRecipientKeys(recipientPublicKeys)
                .build();
    }

    private List<byte[]> buildRecipientMasterKeys(
            final PublicKey senderPublicKey,
            final List<PublicKey> recipientPublicKeys,
            final Nonce recipientNonce,
            final MasterKey masterKey) {
        final PrivateKey privateKey = keyManager.getPrivateKeyForPublicKey(senderPublicKey);

        return recipientPublicKeys.stream()
                .map(publicKey -> encryptor.computeSharedKey(publicKey, privateKey))
                .map(sharedKey -> encryptor.sealAfterPrecomputation(masterKey.getKeyBytes(), recipientNonce, sharedKey))
                .collect(Collectors.toList());
    }

    @Override
    public RawTransaction encryptRawPayload(byte[] message, PublicKey sender) {
        final MasterKey masterKey = encryptor.createMasterKey();
        final Nonce nonce = encryptor.randomNonce();

        final byte[] cipherText = encryptor.sealAfterPrecomputation(message, nonce, masterKey);

        final PrivateKey privateKey = keyManager.getPrivateKeyForPublicKey(sender);

        // TODO NL - check if it makes sense to compute a shared key from the public and private parts of the same key
        SharedKey sharedKey = encryptor.computeSharedKey(sender, privateKey);
        final byte[] encryptedMasterKey = encryptor.sealAfterPrecomputation(masterKey.getKeyBytes(), nonce, sharedKey);

        return new RawTransaction(cipherText, encryptedMasterKey, nonce, sender);
    }

    @Override
    public byte[] unencryptTransaction(EncodedPayload payload, final PublicKey providedSenderKey) {

        final PublicKey senderPubKey;

        final PublicKey recipientPubKey;

        if (!this.getPublicKeys().contains(payload.getSenderKey())) {
            // This is a payload originally sent to us by another node
            senderPubKey = providedSenderKey;
            recipientPubKey = payload.getSenderKey();
        } else {
            // This is a payload that originated from us
            senderPubKey = payload.getSenderKey();
            recipientPubKey = payload.getRecipientKeys().get(0);
        }

        final PrivateKey senderPrivKey = keyManager.getPrivateKeyForPublicKey(senderPubKey);

        final SharedKey sharedKey = encryptor.computeSharedKey(recipientPubKey, senderPrivKey);

        final RecipientBox recipientBox = payload.getRecipientBoxes().iterator().next();

        final Nonce recipientNonce = payload.getRecipientNonce();

        final byte[] masterKeyBytes =
                encryptor.openAfterPrecomputation(recipientBox.getData(), recipientNonce, sharedKey);

        final MasterKey masterKey = MasterKey.from(masterKeyBytes);

        final byte[] cipherText = payload.getCipherText();
        final Nonce cipherTextNonce = payload.getCipherTextNonce();

        return encryptor.openAfterPrecomputation(cipherText, cipherTextNonce, masterKey);
    }

    private MasterKey getMasterKey(PublicKey recipient, PublicKey sender, Nonce nonce, byte[] encryptedKey) {

        final SharedKey sharedKey = encryptor.computeSharedKey(recipient, keyManager.getPrivateKeyForPublicKey(sender));

        final byte[] masterKeyBytes = encryptor.openAfterPrecomputation(encryptedKey, nonce, sharedKey);

        return MasterKey.from(masterKeyBytes);
    }

    @Override
    public PublicKey defaultPublicKey() {
        return keyManager.defaultPublicKey();
    }

    @Override
    public Set<PublicKey> getForwardingKeys() {
        return keyManager.getForwardingKeys();
    }

    @Override
    public Set<PublicKey> getPublicKeys() {
        return keyManager.getPublicKeys();
    }

    @Override
    public Status status() {
        return Status.STARTED;
    }
}
