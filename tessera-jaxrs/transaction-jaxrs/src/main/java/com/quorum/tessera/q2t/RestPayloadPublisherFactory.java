package com.quorum.tessera.q2t;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import com.quorum.tessera.transaction.publish.PayloadPublisherFactory;

import javax.ws.rs.client.Client;

public class RestPayloadPublisherFactory implements PayloadPublisherFactory {

    @Override
    public PayloadPublisher create(Config config) {

        Discovery partyInfoService = Discovery.getInstance();

        ClientFactory clientFactory = new ClientFactory();
        Client client = clientFactory.buildFrom(config.getP2PServerConfig());
        return new RestPayloadPublisher(client, partyInfoService);
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.REST;
    }
}
