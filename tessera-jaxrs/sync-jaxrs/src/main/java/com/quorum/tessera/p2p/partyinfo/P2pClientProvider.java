package com.quorum.tessera.p2p.partyinfo;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import com.quorum.tessera.partyinfo.P2pClient;
import com.quorum.tessera.ssl.context.ClientSSLContextFactory;
import com.quorum.tessera.ssl.context.SSLContextFactory;
import jakarta.ws.rs.client.Client;

public class P2pClientProvider {

  public static P2pClient provider() {
    Config config = ConfigFactory.create().getConfig();

    SSLContextFactory clientSSLContextFactory = ClientSSLContextFactory.create();

    ClientFactory clientFactory = new ClientFactory(clientSSLContextFactory);
    Client client = clientFactory.buildFrom(config.getP2PServerConfig());

    return new RestP2pClient(client);
  }
}
