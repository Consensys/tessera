
package com.quorum.tessera.sync;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.partyinfo.ResendBatchPublisher;
import com.quorum.tessera.partyinfo.ResendBatchPublisherFactory;


public class WebsocketResendBatchPublisherFactory implements ResendBatchPublisherFactory {

    @Override
    public ResendBatchPublisher create(Config config) {
       return new WebsocketResendBatchPublisher();
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.WEB_SOCKET;
    }
    
}
