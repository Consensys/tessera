package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.*;
import com.quorum.tessera.nacl.NaclException;
import com.quorum.tessera.nacl.NaclFacade;
import com.quorum.tessera.nacl.Nonce;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

public class EnclaveImpl implements Enclave {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveImpl.class);

    private final NaclFacade nacl;

    private final KeyManager keyManager;

    public EnclaveImpl(NaclFacade nacl, KeyManager keyManager) {
        this.nacl = Objects.requireNonNull(nacl);
        this.keyManager = Objects.requireNonNull(keyManager);
    }

    @Override
    public EncodedPayload encryptPayload(
            final byte[] message,
            final PublicKey senderPublicKey,
            final List<PublicKey> recipientPublicKeys,
            final PrivacyMode privacyMode,
            final Map<TxHash, EncodedPayload> affectedContractTransactions,
            final byte[] execHash) {
        final MasterKey masterKey = nacl.createMasterKey();
        final Nonce nonce = nacl.randomNonce();
        final Nonce recipientNonce = nacl.randomNonce();

        final byte[] cipherText = nacl.sealAfterPrecomputation(message, nonce, masterKey);

        final List<byte[]> encryptedMasterKeys =
                buildRecipientMasterKeys(senderPublicKey, recipientPublicKeys, recipientNonce, masterKey);

        final Map<TxHash, byte[]> affectedContractTransactionHashes =
                buildAffectedContractTransactionHashes(affectedContractTransactions, cipherText);

        return new EncodedPayload(
                senderPublicKey,
                cipherText,
                nonce,
                encryptedMasterKeys,
                recipientNonce,
                recipientPublicKeys,
                privacyMode,
                affectedContractTransactionHashes,
                execHash);
    }

    @Override
    public Set<TxHash> findInvalidSecurityHashes(
            EncodedPayload encodedPayload, Map<TxHash, EncodedPayload> affectedContractTransactions) {
        return encodedPayload.getAffectedContractTransactions().entrySet().stream()
                .filter(
                        entry -> {
                            // TODO - remove extra logs
                            LOGGER.info("Verifying hash for TxKey {}", entry.getKey().encodeToBase64());
                            EncodedPayload affectedTransaction = affectedContractTransactions.get(entry.getKey());
                            if (null == affectedTransaction) {
                                return true;
                            }
                            byte[] calculatedHash =
                                    computeAffectedContractTransactionHash(
                                            encodedPayload.getCipherText(), affectedTransaction);
                            return !Arrays.equals(entry.getValue(), calculatedHash);
                        })
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private Map<TxHash, byte[]> buildAffectedContractTransactionHashes(
            Map<TxHash, EncodedPayload> affectedContractTransactions, byte[] cipherText) {
        Map<TxHash, byte[]> affectedContractTransactionHashes = new HashMap<>();
        for (final Map.Entry<TxHash, EncodedPayload> entry : affectedContractTransactions.entrySet()) {
            // TODO - remove extra logs
            LOGGER.info("Calculating hash for TxKey {}", entry.getKey().encodeToBase64());
            affectedContractTransactionHashes.put(
                    entry.getKey(), computeAffectedContractTransactionHash(cipherText, entry.getValue()));
        }
        return affectedContractTransactionHashes;
    }

    private byte[] computeAffectedContractTransactionHash(byte[] cipherText, EncodedPayload affectedTransaction) {
        MasterKey masterKey = getMasterKey(affectedTransaction);
        return computeCAHash(cipherText, affectedTransaction.getCipherText(), masterKey);
    }

    private byte[] computeCAHash(byte[] c1, byte[] c2, MasterKey masterKey) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(c1.length + c2.length + masterKey.getKeyBytes().length);
        byteBuffer.put(c1);
        byteBuffer.put(c2);
        byteBuffer.put(masterKey.getKeyBytes());

        final SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest512();
        return digestSHA3.digest(byteBuffer.array());
    }

    @Override
    public byte[] createNewRecipientBox(final EncodedPayload payload, final PublicKey publicKey) {

        if (payload.getRecipientKeys().isEmpty() || payload.getRecipientBoxes().isEmpty()) {
            throw new RuntimeException("No key or recipient-box to use");
        }

        final MasterKey master =
                this.getMasterKey(
                        payload.getRecipientKeys().get(0), payload.getSenderKey(),
                        payload.getRecipientNonce(), payload.getRecipientBoxes().get(0));

        final List<byte[]> sealedMasterKeyList =
                this.buildRecipientMasterKeys(
                        payload.getSenderKey(), singletonList(publicKey), payload.getRecipientNonce(), master);

        return sealedMasterKeyList.get(0);
    }

    @Override
    public EncodedPayload encryptPayload(
            final RawTransaction rawTransaction,
            final List<PublicKey> recipientPublicKeys,
            final PrivacyMode privacyMode,
            final Map<TxHash, EncodedPayload> affectedContractTransactions,
            final byte[] execHash) {
        final MasterKey masterKey =
                this.getMasterKey(
                        rawTransaction.getFrom(), rawTransaction.getFrom(),
                        rawTransaction.getNonce(), rawTransaction.getEncryptedKey());

        final Nonce recipientNonce = nacl.randomNonce();
        final List<byte[]> encryptedMasterKeys =
                buildRecipientMasterKeys(rawTransaction.getFrom(), recipientPublicKeys, recipientNonce, masterKey);

        final Map<TxHash, byte[]> affectedContractTransactionHashes =
                buildAffectedContractTransactionHashes(
                        affectedContractTransactions, rawTransaction.getEncryptedPayload());

        return new EncodedPayload(
                rawTransaction.getFrom(),
                rawTransaction.getEncryptedPayload(),
                rawTransaction.getNonce(),
                encryptedMasterKeys,
                recipientNonce,
                recipientPublicKeys,
                privacyMode,
                affectedContractTransactionHashes,
                execHash);
    }

    private List<byte[]> buildRecipientMasterKeys(
            final PublicKey senderPublicKey,
            final List<PublicKey> recipientPublicKeys,
            final Nonce recipientNonce,
            final MasterKey masterKey) {
        final PrivateKey privateKey = keyManager.getPrivateKeyForPublicKey(senderPublicKey);

        return recipientPublicKeys.stream()
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

        final SharedKey sharedKey = nacl.computeSharedKey(recipientPubKey, senderPrivKey);

        final byte[] recipientBox = payload.getRecipientBoxes().iterator().next();

        final Nonce recipientNonce = payload.getRecipientNonce();

        final byte[] masterKeyBytes = nacl.openAfterPrecomputation(recipientBox, recipientNonce, sharedKey);

        final MasterKey masterKey = MasterKey.from(masterKeyBytes);

        final byte[] cipherText = payload.getCipherText();
        final Nonce cipherTextNonce = payload.getCipherTextNonce();

        return nacl.openAfterPrecomputation(cipherText, cipherTextNonce, masterKey);
    }

    @Override
    public byte[] unencryptRawPayload(RawTransaction payload) {

        final PrivateKey senderPrivKey = keyManager.getPrivateKeyForPublicKey(payload.getFrom());

        final SharedKey sharedKey = nacl.computeSharedKey(payload.getFrom(), senderPrivKey);

        final byte[] recipientBox = payload.getEncryptedKey();

        final Nonce recipientNonce = payload.getNonce();

        final byte[] masterKeyBytes = nacl.openAfterPrecomputation(recipientBox, recipientNonce, sharedKey);

        final MasterKey masterKey = MasterKey.from(masterKeyBytes);

        final byte[] cipherText = payload.getEncryptedPayload();
        final Nonce cipherTextNonce = payload.getNonce();

        return nacl.openAfterPrecomputation(cipherText, cipherTextNonce, masterKey);
    }

    private MasterKey getMasterKey(PublicKey recipient, PublicKey sender, Nonce nonce, byte[] encryptedKey) {

        final SharedKey sharedKey = nacl.computeSharedKey(recipient, keyManager.getPrivateKeyForPublicKey(sender));

        final byte[] masterKeyBytes = nacl.openAfterPrecomputation(encryptedKey, nonce, sharedKey);

        return MasterKey.from(masterKeyBytes);
    }

    private MasterKey getMasterKey(EncodedPayload encodedPayload) {

        final PublicKey senderPubKey;

        final PublicKey recipientPubKey;

        if (encodedPayload.getRecipientBoxes().isEmpty()) {
            throw new RuntimeException("An EncodedPayload should have at least one recipient box.");
        }

        final byte[] recipientBox = encodedPayload.getRecipientBoxes().get(0);

        if (!this.getPublicKeys().contains(encodedPayload.getSenderKey())) {
            // This is a payload originally sent to us by another node
            recipientPubKey = encodedPayload.getSenderKey();
            for (final PublicKey potentialMatchingKey : getPublicKeys()) {
                try {
                    return getMasterKey(
                            recipientPubKey, potentialMatchingKey, encodedPayload.getRecipientNonce(), recipientBox);
                } catch (NaclException ex) {
                    LOGGER.debug("Attempted payload decryption using wrong key, discarding.", ex);
                }
            }
            throw new RuntimeException("Unable to decrypt master key");
        }
        // This is a payload that originated from us
        senderPubKey = encodedPayload.getSenderKey();
        recipientPubKey = encodedPayload.getRecipientKeys().get(0);

        return getMasterKey(recipientPubKey, senderPubKey, encodedPayload.getRecipientNonce(), recipientBox);
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
