package com.quorum.tessera.p2p;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import com.quorum.tessera.partyinfo.PayloadPublisher;
import com.quorum.tessera.partyinfo.PayloadPublisherFactory;
import javax.ws.rs.client.Client;

public class RestPayloadPublisherFactory implements PayloadPublisherFactory {

    @Override
    public PayloadPublisher create(Config config) {
        ClientFactory clientFactory = new ClientFactory();
        Client client = clientFactory.buildFrom(config.getP2PServerConfig());
        return new RestPayloadPublisher(client);
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.REST;
    }
}
