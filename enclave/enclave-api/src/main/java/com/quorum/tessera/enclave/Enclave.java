
package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.encryption.RawTransaction;

import java.util.List;
import java.util.Set;


public interface Enclave {
    
    PublicKey defaultPublicKey();

    Set<PublicKey> getForwardingKeys();

    Set<PublicKey> getPublicKeys();
    
    EncodedPayloadWithRecipients encryptPayload(byte[] message,
                                                PublicKey senderPublicKey,
                                                List<PublicKey> recipientPublicKeys);

    EncodedPayloadWithRecipients encryptPayload(RawTransaction rawTransaction,
                                                List<PublicKey> recipientPublicKeys);

    RawTransaction encryptRawPayload(byte[] message, PublicKey sender);
    
    byte[] unencryptTransaction(EncodedPayloadWithRecipients payloadWithRecipients, PublicKey providedKey);
    

}
