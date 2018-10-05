
package com.quorum.tessera.transaction;

import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.transaction.model.EncodedPayloadWithRecipients;


public interface PayloadPublisher {
    
    void publishPayload(EncodedPayloadWithRecipients encodedPayloadWithRecipients,Key recipientKey);
    
}
