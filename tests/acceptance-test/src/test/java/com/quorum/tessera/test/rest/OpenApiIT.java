package com.quorum.tessera.test.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class OpenApiIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(OpenApiIT.class);

  private Client client;

  private Party node;

  @Rule public TestName testName = new TestName();

  @Before
  public void setUp() {
    PartyHelper partyHelper = PartyHelper.create();
    node = partyHelper.getParties().findFirst().get();
    client = node.getRestClient();
    LOGGER.debug("Begin test: {}", testName.getMethodName());
  }

  @After
  public void after() {
    LOGGER.debug("After test: {}", testName.getMethodName());
    client.close();
  }

  @Test
  public void openapiJson() throws IOException {
    final List<URI> allUris = List.of(node.getQ2TUri(), node.getP2PUri());

    for (URI u : allUris) {
      LOGGER.debug("json: {}/api", u);
      final Response resp =
          client.target(u).path("/api").request(MediaType.APPLICATION_JSON_TYPE).get();
      final String body = resp.readEntity(String.class);
      LOGGER.debug("openapi document {}", body);
      assertThat(resp.getStatus()).isEqualTo(200);
      assertThat(resp.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
      assertThat(body).isNotEmpty();

      try (Reader reader = new StringReader(body)) {
        JsonObject result = Json.createReader(reader).readObject();

        assertThat(result).isNotEmpty();
      }
    }
  }

  @Test
  public void openapiYaml() {
    final List<URI> allUris = List.of(node.getQ2TUri(), node.getP2PUri());

    final MediaType applicationYamlType = new MediaType("application", "yaml");

    for (URI u : allUris) {
      LOGGER.debug("yaml: {}/api", u);
      final Response resp = client.target(u).path("/api").request(applicationYamlType).get();
      final String body = resp.readEntity(String.class);
      LOGGER.debug("openapi document {}", body);
      assertThat(resp.getStatus()).isEqualTo(200);
      assertThat(resp.getMediaType()).isEqualTo(applicationYamlType);
      assertThat(body).isNotEmpty();

      Yaml yaml = new Yaml();
      Object result = yaml.load(body);

      assertThat(result).isNotNull();
    }
  }

  @Test
  public void openapiUnsupportedAccepts() {
    final List<URI> allUris = List.of(node.getQ2TUri(), node.getP2PUri());

    for (URI u : allUris) {
      LOGGER.info("json: {}/api", u);
      final Response resp =
          client.target(u).path("/api").request(MediaType.APPLICATION_OCTET_STREAM_TYPE).get();

      assertThat(resp.getStatus()).isEqualTo(400);
    }
  }
}
