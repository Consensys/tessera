package com.quorum.tessera.test.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import jakarta.json.JsonArray;
import jakarta.json.JsonString;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Test;

public class VersionIT {

  private PartyHelper partyHelper = PartyHelper.create();

  @Test
  public void getVersion() {

    partyHelper
        .getParties()
        .forEach(
            p -> {
              p.getConfig().getServerConfigs().stream()
                  .filter(serverConfig -> serverConfig.getApp() != AppType.ENCLAVE)
                  .map(ServerConfig::getServerUri)
                  .forEach(
                      u -> {
                        Client c = p.getRestClient();
                        final String version =
                            c.target(u).path("/version").request().get(String.class);
                        assertThat(version)
                            .isEqualTo(System.getProperty("project.version", "FIXME"));
                      });
            });
  }

  @Test
  public void getDistributionVersion() {

    partyHelper
        .getParties()
        .forEach(
            p -> {
              p.getConfig().getServerConfigs().stream()
                  .filter(serverConfig -> serverConfig.getApp() != AppType.ENCLAVE)
                  .map(ServerConfig::getServerUri)
                  .forEach(
                      u -> {
                        Client c = p.getRestClient();
                        final String version =
                            c.target(u).path("/version/distribution").request().get(String.class);
                        assertThat(version).isEqualTo(System.getProperty("project.version"));
                      });
            });
  }

  @Test
  public void getSupportedVersions() {

    Map<URI, Party> uriPartyPairs =
        partyHelper
            .getParties()
            .map(
                p ->
                    Map.of(
                        p.getQ2TUri(), p,
                        p.getP2PUri(), p))
            .flatMap(m -> m.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    uriPartyPairs
        .entrySet()
        .forEach(
            pair -> {
              URI u = pair.getKey();
              Party party = pair.getValue();
              Response response =
                  party.getRestClient().target(u).path("version").path("api").request().get();
              assertThat(response.getStatus())
                  .describedAs("%s should return status 200", u)
                  .isEqualTo(200);

              JsonArray versions = response.readEntity(JsonArray.class);
              assertThat(
                      versions.stream()
                          .map(JsonString.class::cast)
                          .map(JsonString::getString)
                          .toArray(String[]::new))
                  .describedAs("%s/version/api should return 1.0, 2.0, 2.1, 3.0, 4.0, 5.0", u)
                  .containsExactly("1.0", "2.0", "2.1", "3.0", "4.0", "5.0");
            });
  }
}
