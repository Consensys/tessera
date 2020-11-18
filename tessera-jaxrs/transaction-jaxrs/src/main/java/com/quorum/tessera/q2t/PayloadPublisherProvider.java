package com.quorum.tessera.q2t;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import com.quorum.tessera.transaction.publish.PayloadPublisher;

import javax.ws.rs.client.Client;

public class PayloadPublisherProvider {

    public static PayloadPublisher provider() {

        Config config = ConfigFactory.create().getConfig();
        Discovery partyInfoService = Discovery.create();

        ClientFactory clientFactory = new ClientFactory();
        Client client = clientFactory.buildFrom(config.getP2PServerConfig());

        return new RestPayloadPublisher(client, partyInfoService);
    }

}
