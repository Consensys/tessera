package com.quorum.tessera.partyinfo;

import com.quorum.tessera.enclave.EncodedPayload;

/** Handles resend requests where the response has one of our own keys as the sender */
public interface ResendManager {

    /**
     * Creates or updates an {@link com.quorum.tessera.transaction.model.EncryptedTransaction} based on
     * whether the node already contains the given transaction or not.
     * If it contains the transaction, the recipient list is updated. If it
     * does not contain the transaction, a new one is created with an initial
     * recipient list of itself and the given recipient of the incoming message.
     *
     * @param transactionPayload the transaction to be stored
     */
    void acceptOwnMessage(EncodedPayload transactionPayload);
}
