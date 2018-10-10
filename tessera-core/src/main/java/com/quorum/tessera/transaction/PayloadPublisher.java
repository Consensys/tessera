
package com.quorum.tessera.transaction;

import com.quorum.tessera.encryption.EncodedPayloadWithRecipients;
import com.quorum.tessera.encryption.PublicKey;


public interface PayloadPublisher {
    
    void publishPayload(EncodedPayloadWithRecipients encodedPayloadWithRecipients,PublicKey recipientKey);
    
}
