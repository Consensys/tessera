
package com.quorum.tessera.transaction;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.model.EncodedPayloadWithRecipients;


public interface PayloadPublisher {
    
    void publishPayload(EncodedPayloadWithRecipients encodedPayloadWithRecipients,PublicKey recipientKey);
    
}
