
package com.quorum.tessera.transaction;

import com.quorum.tessera.key.PublicKey;
import com.quorum.tessera.transaction.model.EncodedPayloadWithRecipients;


public interface PayloadPublisher {
    
    void publishPayload(EncodedPayloadWithRecipients encodedPayloadWithRecipients,PublicKey recipientKey);
    
}
