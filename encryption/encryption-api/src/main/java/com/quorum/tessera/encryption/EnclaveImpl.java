package com.quorum.tessera.encryption;

import com.quorum.tessera.nacl.NaclFacade;
import com.quorum.tessera.nacl.Nonce;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class EnclaveImpl implements Enclave {

    private final NaclFacade nacl;

    private final KeyManager keyManager;

    public EnclaveImpl(NaclFacade nacl, KeyManager keyManager) {
        this.nacl = Objects.requireNonNull(nacl);
        this.keyManager = Objects.requireNonNull(keyManager);
    }

    @Override
    public EncodedPayloadWithRecipients encryptPayload(final byte[] message,
                                                       final PublicKey senderPublicKey,
                                                       final List<PublicKey> recipientPublicKeys) {
        final MasterKey masterKey = nacl.createMasterKey();
        final Nonce nonce = nacl.randomNonce();
        final Nonce recipientNonce = nacl.randomNonce();

        final byte[] cipherText = nacl.sealAfterPrecomputation(message, nonce, masterKey);

        final List<byte[]> encryptedMasterKeys = buildRecipientMasterKeys(senderPublicKey, recipientPublicKeys,
            recipientNonce, masterKey);

        return new EncodedPayloadWithRecipients(
            new EncodedPayload(senderPublicKey, cipherText, nonce, encryptedMasterKeys, recipientNonce),
            recipientPublicKeys
        );
    }

    @Override
    public EncodedPayloadWithRecipients encryptPayload(final RawTransaction rawTransaction,
                                                       final List<PublicKey> recipientPublicKeys) {
        final MasterKey masterKey = getMasterKey(rawTransaction);
        final Nonce recipientNonce = nacl.randomNonce();
        final List<byte[]> encryptedMasterKeys = buildRecipientMasterKeys(rawTransaction.getFrom(), recipientPublicKeys,
            recipientNonce, masterKey);
        return new EncodedPayloadWithRecipients(
            new EncodedPayload(rawTransaction.getFrom(), rawTransaction.getEncryptedPayload(),
                rawTransaction.getNonce(), encryptedMasterKeys, recipientNonce),
            recipientPublicKeys
        );
    }

    private List<byte[]> buildRecipientMasterKeys(final PublicKey senderPublicKey,
                                                  final List<PublicKey> recipientPublicKeys,
                                                  final Nonce recipientNonce,
                                                  final MasterKey masterKey){
        final PrivateKey privateKey = keyManager.getPrivateKeyForPublicKey(senderPublicKey);

        return recipientPublicKeys
            .stream()
            .map(publicKey -> nacl.computeSharedKey(publicKey, privateKey))
            .map(sharedKey -> nacl.sealAfterPrecomputation(masterKey.getKeyBytes(), recipientNonce, sharedKey))
            .collect(Collectors.toList());
    }

    @Override
    public RawTransaction encryptRawPayload(byte[] message, PublicKey sender) {
        final MasterKey masterKey = nacl.createMasterKey();
        final Nonce nonce = nacl.randomNonce();

        final byte[] cipherText = nacl.sealAfterPrecomputation(message, nonce, masterKey);

        final PrivateKey privateKey = keyManager.getPrivateKeyForPublicKey(sender);

        // TODO NL - check if it makes sense to compute a shared key from the public and private parts of the same key
        SharedKey sharedKey = nacl.computeSharedKey(sender, privateKey);
        final byte[] encryptedMasterKey = nacl.sealAfterPrecomputation(masterKey.getKeyBytes(), nonce, sharedKey);

        return new RawTransaction(cipherText, encryptedMasterKey, nonce, sender);
    }


    @Override
    public byte[] unencryptTransaction(EncodedPayloadWithRecipients payloadWithRecipients, final PublicKey providedSenderKey) {

        EncodedPayload encodedPayload = payloadWithRecipients.getEncodedPayload();

        final PublicKey senderPubKey;

        final PublicKey recipientPubKey;

        if (!keyManager.getPublicKeys().contains(encodedPayload.getSenderKey())) {
            // This is a payload originally sent to us by another node
            senderPubKey = providedSenderKey;
            recipientPubKey = encodedPayload.getSenderKey();
        } else {
            // This is a payload that originated from us
            senderPubKey = encodedPayload.getSenderKey();
            recipientPubKey = payloadWithRecipients.getRecipientKeys().get(0);
        }

        final PrivateKey senderPrivKey = keyManager.getPrivateKeyForPublicKey(senderPubKey);

        final SharedKey sharedKey = nacl.computeSharedKey(recipientPubKey, senderPrivKey);

        final byte[] recipientBox = encodedPayload.getRecipientBoxes().iterator().next();

        final Nonce recipientNonce = encodedPayload.getRecipientNonce();

        final byte[] masterKeyBytes = nacl.openAfterPrecomputation(recipientBox, recipientNonce, sharedKey);

        final MasterKey masterKey = MasterKey.from(masterKeyBytes);

        final byte[] cipherText = encodedPayload.getCipherText();
        final Nonce cipherTextNonce = encodedPayload.getCipherTextNonce();

        return nacl.openAfterPrecomputation(cipherText, cipherTextNonce, masterKey);

    }

    private MasterKey getMasterKey(RawTransaction rawTransaction) {

        final SharedKey sharedKey = nacl.computeSharedKey(rawTransaction.getFrom(),
            keyManager.getPrivateKeyForPublicKey(rawTransaction.getFrom()));

        final Nonce nonce = rawTransaction.getNonce();

        final byte[] masterKeyBytes = nacl.openAfterPrecomputation(rawTransaction.getEncryptedKey(), nonce, sharedKey);

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
}
