package com.quorum.tessera.test.rest;

import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import org.junit.*;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;

public class P2PRestAppIT {

    private final Client client = ClientBuilder.newClient();

    private static final Logger LOGGER = LoggerFactory.getLogger(P2PRestAppIT.class);

    private Party actor;

    @Rule public TestName testName = new TestName();

    @Before
    public void beforeTest() {
        this.actor = PartyHelper.create().getParties().findFirst().get();

        LOGGER.debug("Begin test: {}", testName.getMethodName());
    }

    @After
    public void afterTest() {
        LOGGER.debug("After test: {}", testName.getMethodName());
    }

    @Ignore
    @Test
    public void partyInfo() {

        InputStream data = new ByteArrayInputStream("SOMEDATA".getBytes());

        javax.ws.rs.core.Response response =
                client.target(actor.getP2PUri())
                        .path("/partyinfo")
                        .request()
                        .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void upcheck() {
        javax.ws.rs.core.Response response = client.target(actor.getP2PUri()).path("/upcheck").request().get();

        assertThat(response).isNotNull();
        assertThat(response.readEntity(String.class)).isEqualTo("I'm up!");
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void requestOpenApiSchemaJson() throws IOException {

        javax.ws.rs.core.Response response =
                client.target(actor.getP2PUri()).path("/api").request(MediaType.APPLICATION_JSON).get();

        assertThat(response).isNotNull();
        String body = response.readEntity(String.class);
        LOGGER.debug("Schema {}", body);

        assertThat(body).isNotEmpty();
        assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
        assertThat(response.getStatus()).isEqualTo(200);

        try (Reader reader = new StringReader(body)) {
            JsonObject result = Json.createReader(reader).readObject();

            assertThat(result).isNotEmpty();
        }
    }

    @Test
    public void requestOpenApiSchemaYaml() throws IOException {

        javax.ws.rs.core.Response response =
                client.target(actor.getP2PUri()).path("/api").request("application/yaml").get();

        assertThat(response).isNotNull();
        String body = response.readEntity(String.class);
        LOGGER.info("Schema {}", body);

        assertThat(body).isNotEmpty();
        assertThat(response.getMediaType()).isEqualTo(new MediaType("application", "yaml"));
        assertThat(response.getStatus()).isEqualTo(200);

        Yaml yaml = new Yaml();
        Object result = yaml.load(body);

        assertThat(result).isNotNull();
    }
}
