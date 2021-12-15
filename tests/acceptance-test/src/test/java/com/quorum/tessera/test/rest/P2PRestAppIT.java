package com.quorum.tessera.test.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.*;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class P2PRestAppIT {

  private Client client;

  private static final Logger LOGGER = LoggerFactory.getLogger(P2PRestAppIT.class);

  private Party actor;

  @Rule public TestName testName = new TestName();

  @Before
  public void beforeTest() {
    this.actor = PartyHelper.create().getParties().findFirst().get();
    client = actor.getRestClient();
    LOGGER.debug("Begin test: {}", testName.getMethodName());
  }

  @After
  public void afterTest() {
    LOGGER.debug("After test: {}", testName.getMethodName());
    client.close();
  }

  @Ignore
  @Test
  public void partyInfo() {

    InputStream data = new ByteArrayInputStream("SOMEDATA".getBytes());

    jakarta.ws.rs.core.Response response =
        client
            .target(actor.getP2PUri())
            .path("/partyinfo")
            .request()
            .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(201);
  }

  @Test
  public void upcheck() {
    jakarta.ws.rs.core.Response response =
        client.target(actor.getP2PUri()).path("/upcheck").request().get();

    assertThat(response).isNotNull();
    assertThat(response.readEntity(String.class)).isEqualTo("I'm up!");
    assertThat(response.getStatus()).isEqualTo(200);
  }
}
