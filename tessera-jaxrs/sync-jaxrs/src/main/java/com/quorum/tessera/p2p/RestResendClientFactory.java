package com.quorum.tessera.p2p;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import com.quorum.tessera.ssl.context.ClientSSLContextFactory;
import com.quorum.tessera.ssl.context.SSLContextFactory;
import com.quorum.tessera.sync.ResendClient;
import com.quorum.tessera.sync.ResendClientFactory;

import javax.ws.rs.client.Client;

public class RestResendClientFactory implements ResendClientFactory {

    public ResendClient create(Config config) {

        SSLContextFactory clientSSLContextFactory = ClientSSLContextFactory.create();

        ClientFactory clientFactory = new ClientFactory(clientSSLContextFactory);
        Client client = clientFactory.buildFrom(config.getP2PServerConfig());

        client.property("jersey.config.client.readTimeout", "300000");
        return new RestResendClient(client);
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.REST;
    }
}
