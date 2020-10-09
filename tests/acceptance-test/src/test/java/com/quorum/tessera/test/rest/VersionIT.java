package com.quorum.tessera.test.rest;

import com.quorum.tessera.test.PartyHelper;
import org.junit.Test;

import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionIT {

    private final Client client = ClientBuilder.newClient();

    private final PartyHelper partyHelper = PartyHelper.create();

    @Test
    public void getVersion() {
        final List<URI> allUris =
                partyHelper
                        .getParties()
                        .flatMap(p -> Stream.of(p.getQ2TUri(), p.getP2PUri()))
                        .collect(Collectors.toList());

        allUris.forEach(
                u -> {
                    final String version = client.target(u).path("/version").request().get(String.class);
                    assertThat(version).startsWith("0.11.1");
                });
    }

    @Test
    public void getDistributionVersion() {
        final List<URI> allUris =
                partyHelper
                        .getParties()
                        .flatMap(p -> Stream.of(p.getQ2TUri(), p.getP2PUri()))
                        .collect(Collectors.toList());

        allUris.forEach(
                u -> {
                    final String version = client.target(u).path("/version/distribution").request().get(String.class);
                    assertThat(version).startsWith("0.11.1");
                });
    }

    @Test
    public void getSupportedVersions() {
        final List<URI> allUris =
                partyHelper
                        .getParties()
                        .flatMap(p -> Stream.of(p.getQ2TUri(), p.getP2PUri()))
                        .collect(Collectors.toList());

        final String expectedVersionResponse = "{\"versions\":[{\"version\":\"1.0\"},{\"version\":\"2.0\"}]}";
        allUris.forEach(
                u -> {
                    final JsonObject versions = client.target(u).path("/version/api").request().get(JsonObject.class);
                    assertThat(versions.toString()).isEqualTo(expectedVersionResponse);
                });
    }
}
