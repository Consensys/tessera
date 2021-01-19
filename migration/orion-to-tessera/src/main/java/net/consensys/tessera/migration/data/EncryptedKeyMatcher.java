package net.consensys.tessera.migration.data;

import com.quorum.tessera.encryption.*;
import net.consensys.orion.enclave.EncryptedKey;
import net.consensys.orion.enclave.EncryptedPayload;
import net.consensys.tessera.migration.OrionKeyHelper;
import org.apache.tuweni.crypto.sodium.Box;

import java.util.*;
import java.util.stream.Collectors;

public class EncryptedKeyMatcher {

    private final OrionKeyHelper orionKeyHelper;

    private final Encryptor tesseraEncryptor;

    public EncryptedKeyMatcher(final OrionKeyHelper orionKeyHelper, final Encryptor tesseraEncryptor) {
        this.orionKeyHelper = Objects.requireNonNull(orionKeyHelper);
        this.tesseraEncryptor = Objects.requireNonNull(tesseraEncryptor);
    }

    public List<PublicKey> match(final EncryptedPayload transaction, final List<String> privacyGroupAddresses) {
        final boolean weAreSender =
                orionKeyHelper.getKeyPairs().stream()
                        .map(Box.KeyPair::publicKey)
                        .anyMatch(k -> Objects.equals(k, transaction.sender()));

        if (weAreSender) {
            return handleWhenSender(transaction, privacyGroupAddresses);
        }
        return handleWhenNotSender(transaction, privacyGroupAddresses);
    }

    private List<PublicKey> handleWhenSender(
            final EncryptedPayload transaction, final List<String> privacyGroupAddresses) {
        List<PublicKey> recipientKeys = new ArrayList<>();

        // we are the sender of this tx, so we have all of the encrypted master keys
        // the keys listed in the privacy group should be in the same order as they are
        // used for the EncryptedKey, so just iterate over them to test it
        PrivateKey privateKey =
                orionKeyHelper.getKeyPairs().stream()
                        .filter(kp -> kp.publicKey().equals(transaction.sender()))
                        .findFirst()
                        .map(Box.KeyPair::secretKey)
                        .map(Box.SecretKey::bytesArray)
                        .map(PrivateKey::from)
                        .orElseThrow(() -> new IllegalStateException("local sender key not found"));

        for (int i = 0; i < transaction.encryptedKeys().length; i++) {
            EncryptedKey encryptedKey = transaction.encryptedKeys()[i];

            for (String possibleRecipientPublicKey : privacyGroupAddresses) {
                PublicKey recipientKey = PublicKey.from(Base64.getDecoder().decode(possibleRecipientPublicKey));

                final boolean canDecrypt = canDecrypt(transaction, encryptedKey, recipientKey, privateKey);

                if (canDecrypt) {
                    // hasn't blown up, so must be a success
                    recipientKeys.add(recipientKey);

                    // Found the correct key, no need to keep trying others
                    break;
                }
            }

            // check we actually found a relevant key
            if (recipientKeys.size() != (i + 1)) {
                // TODO: make a proper error
                throw new RuntimeException("could not find a local recipient key to decrypt the payload with");
            }
        }

        return recipientKeys;
    }

    private List<PublicKey> handleWhenNotSender(
            final EncryptedPayload transaction, final List<String> privacyGroupAddresses) {
        List<PublicKey> recipientKeys = new ArrayList<>();

        final PublicKey senderKey =
                Optional.of(transaction.sender()).map(Box.PublicKey::bytesArray).map(PublicKey::from).get();

        // Find the intersection of the privacy groups public keys and our local keys
        // as those are the only keys that are relevant for us now
        final List<String> ourPossibleRecipientKeys = new ArrayList<>(privacyGroupAddresses);
        final List<String> ourPublicKeysBase64 =
                orionKeyHelper.getKeyPairs().stream()
                        .map(Box.KeyPair::publicKey)
                        .map(Box.PublicKey::bytesArray)
                        .map(pkBytes -> Base64.getEncoder().encodeToString(pkBytes))
                        .collect(Collectors.toList());
        ourPossibleRecipientKeys.removeIf(k -> !ourPublicKeysBase64.contains(k));

        for (int i = 0; i < transaction.encryptedKeys().length; i++) {
            EncryptedKey encryptedKey = transaction.encryptedKeys()[i];

            // Try each of the keys to see which one actually
            for (String ourPublicRecipientKey : ourPossibleRecipientKeys) {

                Box.KeyPair keypairUnderTest =
                        orionKeyHelper.getKeyPairs().stream()
                                .filter(
                                        kp ->
                                                Objects.equals(
                                                        Base64.getEncoder().encodeToString(kp.publicKey().bytesArray()),
                                                        ourPublicRecipientKey))
                                .findFirst()
                                .get();
                PublicKey ourPublicKey = PublicKey.from(keypairUnderTest.publicKey().bytesArray());
                PrivateKey ourPrivateKey = PrivateKey.from(keypairUnderTest.secretKey().bytesArray());

                final boolean canDecrypt = canDecrypt(transaction, encryptedKey, senderKey, ourPrivateKey);

                if (canDecrypt) {
                    // hasn't blown up, so must be a success
                    recipientKeys.add(ourPublicKey);

                    // Found the correct key, no need to keep trying others
                    break;
                }
            }

            // check we actually found a relevant key
            if (recipientKeys.size() != (i + 1)) {
                // TODO: make a proper error
                throw new RuntimeException("could not find a local recipient key to decrypt the payload with");
            }
        }

        return recipientKeys;
    }

    private boolean canDecrypt(
            final EncryptedPayload transaction,
            final EncryptedKey encryptedKey,
            final PublicKey publicKey,
            final PrivateKey ourPrivateKey) {

        // "publicKey" may either be the recipient public key (if we were the sender),
        // or the tx sender public key (if we were a recipient)
        System.out.printf(
                "Create sharedKey from %s and %s", publicKey.encodeToBase64(), ourPrivateKey.encodeToBase64());
        System.out.println();
        final SharedKey sharedKey = tesseraEncryptor.computeSharedKey(publicKey, ourPrivateKey);

        final Nonce nonce = new Nonce(transaction.nonce());

        final byte[] decryptedKeyData;
        try {
            decryptedKeyData = tesseraEncryptor.openAfterPrecomputation(encryptedKey.getEncoded(), nonce, sharedKey);
        } catch (EncryptorException e) {
            // Wrong key, keep trying the others.
            return false;
        }

        final SharedKey masterKey = SharedKey.from(decryptedKeyData);

        // this isn't used anywhere, but acts as a sanity check we got all the keys right.
        // TODO: this should not fail, but if it does, do we want to catch the exception or let it blow up?
        tesseraEncryptor.openAfterPrecomputation(transaction.cipherText(), new Nonce(new byte[24]), masterKey);

        return true;
    }
}
