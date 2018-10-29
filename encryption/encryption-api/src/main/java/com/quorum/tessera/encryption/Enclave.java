
package com.quorum.tessera.encryption;

import java.util.List;
import java.util.Set;


public interface Enclave {
    
    PublicKey defaultPublicKey();

    Set<PublicKey> getForwardingKeys();

    Set<PublicKey> getPublicKeys();
    
    EncodedPayloadWithRecipients encryptPayload(byte[] message,
             PublicKey senderPublicKey,
             List<PublicKey> recipientPublicKeys);


    RawTransaction encryptRawPayload(byte[] message, PublicKey sender);
    
    byte[] unencryptTransaction(EncodedPayloadWithRecipients payloadWithRecipients, final PublicKey providedKey);
    

}
