package com.quorum.tessera.sync;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.partyinfo.PayloadPublisher;
import com.quorum.tessera.partyinfo.PayloadPublisherFactory;

public class WebsocketPayloadPublisherFactory implements PayloadPublisherFactory {

    @Override
    public PayloadPublisher create(Config config) {
        return new WebsocketPayloadPublisher(SessionStore.create());
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.WEB_SOCKET;
    }
    
}
