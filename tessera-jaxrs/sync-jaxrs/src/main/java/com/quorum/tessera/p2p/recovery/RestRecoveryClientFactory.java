package com.quorum.tessera.p2p.recovery;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.util.IntervalPropertyHelper;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import com.quorum.tessera.ssl.context.ClientSSLContextFactory;
import com.quorum.tessera.ssl.context.SSLContextFactory;

import javax.ws.rs.client.Client;

public class RestRecoveryClientFactory implements RecoveryClientFactory {

    public RecoveryClient create(final Config config) {
        final String waitTime =
                new IntervalPropertyHelper(config.getP2PServerConfig().getProperties()).resendWaitTime();

        final SSLContextFactory clientSSLContextFactory = ClientSSLContextFactory.create();

        final ClientFactory clientFactory = new ClientFactory(clientSSLContextFactory);
        final Client client = clientFactory.buildFrom(config.getP2PServerConfig());

        client.property("jersey.config.client.readTimeout", waitTime);
        return new RestRecoveryClient(client);
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.REST;
    }
}
