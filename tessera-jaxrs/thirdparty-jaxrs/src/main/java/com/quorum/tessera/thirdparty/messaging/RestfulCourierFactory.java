package com.quorum.tessera.thirdparty.messaging;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import com.quorum.tessera.messaging.Courier;
import com.quorum.tessera.messaging.CourierFactory;
import javax.ws.rs.client.Client;

public class RestfulCourierFactory implements CourierFactory {

  @Override
  public Courier create(Config config) {

    Discovery discovery = Discovery.getInstance();

    ClientFactory clientFactory = new ClientFactory();
    Client client = clientFactory.buildFrom(config.getP2PServerConfig());

    return new RestfulCourier(client, discovery);
  }

  @Override
  public CommunicationType getCommunicationType() {
    return CommunicationType.REST;
  }
}
