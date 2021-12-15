package com.quorum.tessera.server.jersey;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.CrossDomainConfig;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.util.JaxbUtil;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JerseyServerIT {

  private URI serverUri = URI.create("http://localhost:8080");

  private JerseyServer server;

  @Before
  public void onSetUp() throws Exception {

    System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

    ServerConfig serverConfig = new ServerConfig();
    serverConfig.setCommunicationType(CommunicationType.REST);
    serverConfig.setServerAddress("http://localhost:8080");

    CrossDomainConfig crossDomainConfig = new CrossDomainConfig();
    crossDomainConfig.setAllowedOrigins(Arrays.asList("*.acme.com", "*.other.com"));
    serverConfig.setCrossDomainConfig(crossDomainConfig);

    JaxbUtil.marshalWithNoValidation(serverConfig, System.out);

    server = new JerseyServer(serverConfig, SampleApplication.class);

    server.start();
  }

  @After
  public void onTearDown() throws Exception {
    server.stop();
  }

  @Test
  public void ping() {

    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    headers.add("Origin", "*.acme.com");

    Response result =
        ClientBuilder.newClient().target(serverUri).path("ping").request().headers(headers).get();

    assertThat(result.getHeaderString("Access-Control-Allow-Origin")).isEqualTo("*.acme.com");

    assertThat(result.getHeaderString("Access-Control-Allow-Credentials")).isEqualTo("true");

    assertThat(result.getStatus()).isEqualTo(200);
    assertThat(result.readEntity(String.class)).isEqualTo("HEllow");
  }

  @Test
  public void create() {

    SamplePayload payload = new SamplePayload();
    payload.setValue("Hellow");

    Response result =
        ClientBuilder.newClient()
            .target(serverUri)
            .path("create")
            .request()
            .post(Entity.entity(payload, MediaType.APPLICATION_JSON));

    assertThat(result.getStatus()).isEqualTo(201);
    assertThat(result.getLocation()).isNotNull();

    Response result2 =
        ClientBuilder.newClient()
            .target(result.getLocation())
            .request(MediaType.APPLICATION_JSON)
            .get();

    SamplePayload p = result2.readEntity(SamplePayload.class);
    assertThat(p).isNotNull();
    assertThat(p.getValue()).isEqualTo("Hellow");

    Response result3 =
        ClientBuilder.newClient().target(serverUri).path(p.getId()).request().delete();

    assertThat(result3.getStatus()).isEqualTo(200);
    SamplePayload deleted = result3.readEntity(SamplePayload.class);
    assertThat(deleted.getValue()).isEqualTo("Hellow");
  }
}
