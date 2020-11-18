package com.quorum.tessera.p2p.resend;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.util.IntervalPropertyHelper;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import com.quorum.tessera.ssl.context.ClientSSLContextFactory;
import com.quorum.tessera.ssl.context.SSLContextFactory;

import javax.ws.rs.client.Client;

public class ResendClientProvider {

    public static ResendClient provider() {
        final Config config = ConfigFactory.create().getConfig();
        final String resendWaitTime =
            new IntervalPropertyHelper(config.getP2PServerConfig().getProperties()).resendWaitTime();

        final SSLContextFactory clientSSLContextFactory = ClientSSLContextFactory.create();

        final ClientFactory clientFactory = new ClientFactory(clientSSLContextFactory);
        final Client client = clientFactory.buildFrom(config.getP2PServerConfig());

        client.property("jersey.config.client.readTimeout", resendWaitTime);
        return new RestResendClient(client);
    }

}
