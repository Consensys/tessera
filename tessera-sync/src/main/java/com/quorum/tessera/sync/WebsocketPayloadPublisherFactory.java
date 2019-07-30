package com.quorum.tessera.sync;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.partyinfo.PayloadPublisher;
import com.quorum.tessera.partyinfo.PayloadPublisherFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsocketPayloadPublisherFactory implements PayloadPublisherFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketPayloadPublisherFactory.class);

    @Override
    public PayloadPublisher create(Config config) {
        LOGGER.info("Creating PayloadPublisher from {}",config);
        return new WebsocketPayloadPublisher(SessionStore.create());
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.WEB_SOCKET;
    }
    
}
