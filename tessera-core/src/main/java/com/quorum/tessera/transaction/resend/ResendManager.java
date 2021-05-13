package com.quorum.tessera.transaction.resend;

import com.quorum.tessera.enclave.EncodedPayload;
import java.util.ServiceLoader;

/** Handles resend requests where the response has one of our own keys as the sender */
public interface ResendManager {

  /**
   * Creates or updates an {@link com.quorum.tessera.data.EncryptedTransaction} based on whether the
   * node already contains the given transaction or not. If it contains the transaction, the
   * recipient list is updated. If it does not contain the transaction, a new one is created with an
   * initial recipient list of itself and the given recipient of the incoming message.
   *
   * @param transactionPayload the transaction to be stored
   */
  void acceptOwnMessage(EncodedPayload transactionPayload);

  static ResendManager create() {
    return ServiceLoader.load(ResendManager.class).findFirst().get();
  }
}
