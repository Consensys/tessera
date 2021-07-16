package com.quorum.tessera.test.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.junit.Test;

public class MetricsIT {

  @Test
  public void metrics() {
    final PartyHelper partyHelper = PartyHelper.create();

    Set<URI> uris =
        partyHelper
            .getParties()
            .map(Party::getConfig)
            .map(Config::getServerConfigs)
            .flatMap(List::stream)
            .map(ServerConfig::getServerUri)
            .collect(Collectors.toUnmodifiableSet());

    Client client = partyHelper.getParties().findAny().get().getRestClient();

    Set<Response> responses =
        uris.stream()
            .map(client::target)
            .map(t -> t.path("metrics"))
            .map(WebTarget::request)
            .map(Invocation.Builder::get)
            .collect(Collectors.toUnmodifiableSet());

    for (Response response : responses) {
      assertThat(response).isNotNull();
      assertThat(response.getStatus()).isEqualTo(200);
    }
  }
}
