
package com.quorum.tessera.sync;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.partyinfo.ResendBatchPublisher;
import java.util.List;


public class WebsocketResendBatchPublisher implements ResendBatchPublisher {

    @Override
    public void publishBatch(List<EncodedPayload> payload, String targetUrl) {
        //TODO: 
    }
    
}
