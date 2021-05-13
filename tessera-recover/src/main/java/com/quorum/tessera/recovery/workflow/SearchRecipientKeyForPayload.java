package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveNotAvailableException;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.EncryptorException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.exception.RecipientKeyNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchRecipientKeyForPayload implements BatchWorkflowAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(SearchRecipientKeyForPayload.class);

  private final Enclave enclave;

  public SearchRecipientKeyForPayload(final Enclave enclave) {
    this.enclave = Objects.requireNonNull(enclave);
  }

  @Override
  public boolean execute(BatchWorkflowContext event) {
    final Set<EncodedPayload> encodedPayloads = event.getPayloadsToPublish();

    final boolean keysAreEmpty =
        encodedPayloads.stream().map(EncodedPayload::getRecipientKeys).allMatch(List::isEmpty);
    // if the payload has someone in it, then we are good
    if (!keysAreEmpty) {
      return true;
    }

    // the keys are not present, so we need to search for the relevant recipient
    final Set<EncodedPayload> adjustedPayloads =
        encodedPayloads.stream()
            .map(
                payload -> {
                  // this is a pre-PE tx, so find the recipient key
                  final PublicKey recipientKey =
                      searchForRecipientKey(payload)
                          .orElseThrow(
                              () -> {
                                final EncryptedTransaction encryptedTransaction =
                                    event.getEncryptedTransaction();
                                final MessageHash hash = encryptedTransaction.getHash();
                                final String message =
                                    String.format("No key found as recipient of message %s", hash);
                                return new RecipientKeyNotFoundException(message);
                              });

                  return EncodedPayload.Builder.from(payload)
                      .withRecipientKeys(List.of(recipientKey))
                      .build();
                })
            .collect(Collectors.toSet());

    event.setPayloadsToPublish(adjustedPayloads);

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
