package net.consensys.tessera.migration.data;

import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.encryption.PublicKey;
import net.consensys.orion.enclave.EncryptedKey;
import net.consensys.orion.enclave.EncryptedPayload;
import org.apache.tuweni.crypto.sodium.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class EncryptedKeyMatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedKeyMatcher.class);


    private final List<Box.KeyPair> keyPairs;

    private final EncryptorHelper tesseraEncryptor;

    public EncryptedKeyMatcher(final List<Box.KeyPair> keyPairs, final EncryptorHelper tesseraEncryptor) {
        this.tesseraEncryptor = Objects.requireNonNull(tesseraEncryptor);
        this.keyPairs = keyPairs;
    }


    public boolean weAreSender(EncryptedPayload transaction) {
        return keyPairs.stream()
            .map(Box.KeyPair::publicKey)
            .anyMatch(k -> Objects.equals(k, transaction.sender()));
    }

    public List<PublicKey> match(final EncryptedPayload transaction, final List<String> privacyGroupAddresses) {
        final boolean weAreSender = weAreSender(transaction);

        if (weAreSender) {
            return handleWhenSender(transaction, privacyGroupAddresses);
        }
        return handleWhenNotSender(transaction, privacyGroupAddresses);
    }

    public List<PublicKey> handleWhenSender(
            final EncryptedPayload transaction, final List<String> privacyGroupAddresses) {
        List<PublicKey> recipientKeys = new ArrayList<>();

        // we are the sender of this tx, so we have all of the encrypted master keys
        // the keys listed in the privacy group should be in the same order as they are
        // used for the EncryptedKey, so just iterate over them to test it
        PrivateKey privateKey =
            keyPairs.stream()
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

                final boolean canDecrypt = tesseraEncryptor.canDecrypt(transaction, encryptedKey, recipientKey, privateKey);

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



    public Optional<PublicKey> findRecipientKeyWhenNotSenderAndPrivacyGroupNotFound(EncryptedPayload transaction) {

        final PublicKey senderKey =
            Optional.of(transaction.sender())
                .map(Box.PublicKey::bytesArray)
                .map(PublicKey::from)
                .get();

        final List<String> ourPublicKeysBase64 = keyPairs.stream()
                .map(Box.KeyPair::publicKey)
                .map(Box.PublicKey::bytesArray)
                .map(pkBytes -> Base64.getEncoder().encodeToString(pkBytes))
                .collect(Collectors.toList());

        for (int i = 0; i < transaction.encryptedKeys().length; i++) {

            EncryptedKey encryptedKey = transaction.encryptedKeys()[i];
            for (String ourPublicRecipientKey : ourPublicKeysBase64) {
                Box.KeyPair keypairUnderTest = keyPairs.stream()
                        .filter(
                            kp ->
                                Objects.equals(
                                    Base64.getEncoder().encodeToString(kp.publicKey().bytesArray()),
                                    ourPublicRecipientKey))
                        .findFirst()
                        .get();

                PublicKey ourPublicKey = PublicKey.from(keypairUnderTest.publicKey().bytesArray());
                PrivateKey ourPrivateKey = PrivateKey.from(keypairUnderTest.secretKey().bytesArray());

                final boolean canDecrypt = tesseraEncryptor.canDecrypt(transaction, encryptedKey, senderKey, ourPrivateKey);
                if(canDecrypt) {
                    return Optional.of(ourPublicKey);
                }
            }
        }
        return Optional.empty();
    }

    public List<PublicKey> handleWhenNotSender(
            final EncryptedPayload transaction, final List<String> privacyGroupAddresses) {
        List<PublicKey> recipientKeys = new ArrayList<>();

        final PublicKey senderKey =
                Optional.of(transaction.sender()).map(Box.PublicKey::bytesArray).map(PublicKey::from).get();

        // Find the intersection of the privacy groups public keys and our local keys
        // as those are the only keys that are relevant for us now
        final List<String> ourPossibleRecipientKeys = new ArrayList<>(privacyGroupAddresses);

        final List<String> ourPublicKeysBase64 =
            keyPairs.stream()
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
                    keyPairs.stream()
                                .filter(
                                        kp ->
                                                Objects.equals(
                                                        Base64.getEncoder().encodeToString(kp.publicKey().bytesArray()),
                                                        ourPublicRecipientKey))
                                .findFirst()
                                .get();
                PublicKey ourPublicKey = PublicKey.from(keypairUnderTest.publicKey().bytesArray());
                PrivateKey ourPrivateKey = PrivateKey.from(keypairUnderTest.secretKey().bytesArray());

                final boolean canDecrypt = tesseraEncryptor.canDecrypt(transaction, encryptedKey, senderKey, ourPrivateKey);

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



}
