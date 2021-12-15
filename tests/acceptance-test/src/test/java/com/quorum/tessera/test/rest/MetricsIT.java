package com.quorum.tessera.test.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

public class MetricsIT {

  @Test
  public void metrics() {
    final PartyHelper partyHelper = PartyHelper.create();

    Set<ServerConfig> serverConfigs =
        partyHelper
            .getParties()
            .map(Party::getConfig)
            .map(Config::getServerConfigs)
            .flatMap(List::stream)
            .collect(Collectors.toUnmodifiableSet());

    ClientFactory clientFactory = new ClientFactory();
    for (ServerConfig serverConfig : serverConfigs) {
      Client c = clientFactory.buildFrom(serverConfig);
      Response response = c.target(serverConfig.getServerUri()).path("metrics").request().get();
      assertThat(response).isNotNull();
      assertThat(response.getStatus()).isEqualTo(200);
    }
  }
}
