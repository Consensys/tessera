package com.quorum.tessera.p2p;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.partyinfo.ResendBatchPublisher;
import com.quorum.tessera.partyinfo.ResendBatchPublisherFactory;

public class RestResendBatchPublisherFactory implements ResendBatchPublisherFactory {

    @Override
    public ResendBatchPublisher create(Config config) {
        RestResendClientFactory clientFactory = new RestResendClientFactory();
        ResendClient client = clientFactory.create(config);
        return new RestResendBatchPublisher(client);
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.REST;
    }
}
