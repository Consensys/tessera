package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.PublicKey;

import java.util.List;
import java.util.Set;

public interface Enclave {

    PublicKey defaultPublicKey();

    Set<PublicKey> getForwardingKeys();

    Set<PublicKey> getPublicKeys();

    EncodedPayload encryptPayload(byte[] message, PublicKey senderPublicKey, List<PublicKey> recipientPublicKeys);

    EncodedPayload encryptPayload(RawTransaction rawTransaction, List<PublicKey> recipientPublicKeys);

    RawTransaction encryptRawPayload(byte[] message, PublicKey sender);

    byte[] unencryptTransaction(EncodedPayload payload, PublicKey providedKey);

    /**
     * Creates a new recipient box for the payload, for which we must be the originator
     * At least one recipient must already be available to be able to decrypt the master key
     *
     * @param payload      the payload to add a recipient to
     * @param recipientKey the new recipient key to add
     */
    byte[] createNewRecipientBox(EncodedPayload payload, PublicKey recipientKey);

}
