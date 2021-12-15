package com.quorum.tessera.q2t.internal;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisher;
import jakarta.ws.rs.client.Client;

public class PrivacyGroupPublisherProvider {

  public static PrivacyGroupPublisher provider() {

    Discovery discovery = Discovery.create();
    Config config = ConfigFactory.create().getConfig();
    Client client = new ClientFactory().buildFrom(config.getP2PServerConfig());

    return new RestPrivacyGroupPublisher(discovery, client);
  }
}
