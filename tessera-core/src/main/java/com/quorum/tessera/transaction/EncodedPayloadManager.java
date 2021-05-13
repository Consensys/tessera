package com.quorum.tessera.transaction;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import java.util.ServiceLoader;

/**
 * The EncodedPayloadManager handles requests for translating and validating an incoming request to
 * de/encrypt to pass to the enclave.
 */
public interface EncodedPayloadManager {

  /**
   * Validates an encryption request before passing it to the enclave. This includes deduplicating
   * recipients and checking the affected contracts are valid for the given privacy mode.
   *
   * @param request the full encryption request
   * @return the encrypted data, along with all the information needed for the sender or any of the
   *     recipients to decrypt it
   */
  EncodedPayload create(SendRequest request);

  /**
   * Decrypts a given message. Tries all locally available keys if not provided a public decryption
   * key.
   *
   * @param payload the encrypted data
   * @param maybeDefaultRecipient the key to use to decrypt the payload. If provided, only that key
   *     is checked, which may result in decryption failure. If {@code null} then locally available
   *     keys are tested until a match is found.
   * @return the decrypted data, along with the privacy mode of the transaction and affected
   *     contracts/execution hash
   */
  ReceiveResponse decrypt(EncodedPayload payload, PublicKey maybeDefaultRecipient);

  static EncodedPayloadManager create() {
    return ServiceLoader.load(EncodedPayloadManager.class).findFirst().get();
  }
}
