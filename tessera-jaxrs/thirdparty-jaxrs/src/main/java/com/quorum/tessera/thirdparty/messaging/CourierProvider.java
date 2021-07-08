package com.quorum.tessera.thirdparty.messaging;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import com.quorum.tessera.messaging.Courier;
import javax.ws.rs.client.Client;

public class CourierProvider {

  public static Courier provider() {

    Config config = ConfigFactory.create().getConfig();
    Discovery disco = Discovery.create();

    ClientFactory clientFactory = new ClientFactory();
    Client client = clientFactory.buildFrom(config.getP2PServerConfig());

    return new RestfulCourier(client, disco);
  }
}
