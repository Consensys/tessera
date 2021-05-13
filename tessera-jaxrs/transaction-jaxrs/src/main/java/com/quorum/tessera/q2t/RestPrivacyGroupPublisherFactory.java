package com.quorum.tessera.q2t;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisher;
import com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisherFactory;
import javax.ws.rs.client.Client;

public class RestPrivacyGroupPublisherFactory implements PrivacyGroupPublisherFactory {

  @Override
  public PrivacyGroupPublisher create(Config config) {

    Discovery discovery = Discovery.getInstance();
    Client client = new ClientFactory().buildFrom(config.getP2PServerConfig());

    return new RestPrivacyGroupPublisher(discovery, client);
  }
}
