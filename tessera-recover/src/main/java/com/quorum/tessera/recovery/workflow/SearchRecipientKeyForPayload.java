package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveNotAvailableException;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.EncryptorException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.exception.RecipientKeyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class SearchRecipientKeyForPayload implements BatchWorkflowAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchRecipientKeyForPayload.class);

    private Enclave enclave;

    public SearchRecipientKeyForPayload(Enclave enclave) {
        this.enclave = enclave;
    }

    @Override
    public boolean execute(BatchWorkflowContext event) {

        EncodedPayload encodedPayload = event.getEncodedPayload();

        if (!encodedPayload.getRecipientKeys().isEmpty()) {
            return true;
        }

        PublicKey recipientKey =
                searchForRecipientKey(encodedPayload)
                        .orElseThrow(
                                () -> {
                                    EncryptedTransaction encryptedTransaction = event.getEncryptedTransaction();
                                    final MessageHash hash = encryptedTransaction.getHash();
                                    String message = String.format("No key found as recipient of message %s", hash);
                                    return new RecipientKeyNotFoundException(message);
                                });

        EncodedPayload adjustedPayload =
                EncodedPayload.Builder.from(encodedPayload).withRecipientKeys(List.of(recipientKey)).build();

        event.setEncodedPayload(adjustedPayload);

        return true;
    }

    private Optional<PublicKey> searchForRecipientKey(final EncodedPayload payload) {
        for (final PublicKey potentialMatchingKey : enclave.getPublicKeys()) {
            try {
                enclave.unencryptTransaction(payload, potentialMatchingKey);
                return Optional.of(potentialMatchingKey);
            } catch (EnclaveNotAvailableException | IndexOutOfBoundsException | EncryptorException ex) {
                LOGGER.debug("Attempted payload decryption using wrong key, discarding.");
            }
        }
        return Optional.empty();
    }
}
