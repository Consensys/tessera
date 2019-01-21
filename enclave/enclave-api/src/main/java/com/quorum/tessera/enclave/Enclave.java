
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
    

}
