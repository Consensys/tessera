package com.quorum.tessera.test.rest;

import org.junit.*;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.*;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class TesseraIT {

    public static final URI SERVER_URI = UriBuilder.fromUri("http://127.0.0.1").port(8080).build();

    private final Client client = ClientBuilder.newClient();

    private static final Logger LOGGER = LoggerFactory.getLogger(TesseraIT.class);

    @Rule
    public TestName testName = new TestName();

    @Before
    public void beforeTest() {
        LOGGER.info("Begin test: {}", testName.getMethodName());
    }

    @After
    public void afterTest() {
        LOGGER.info("After test: {}", testName.getMethodName());
    }

    @Ignore
    @Test
    public void partyInfo() {

        InputStream data = new ByteArrayInputStream("SOMEDATA".getBytes());

        javax.ws.rs.core.Response response = client.target(SERVER_URI)
                .path("/partyinfo")
                .request()
                .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void upcheck() {
        javax.ws.rs.core.Response response = client.target(SERVER_URI)
                .path("/upcheck")
                .request()
                .get();

        assertThat(response).isNotNull();
        assertThat(response.readEntity(String.class))
                .isEqualTo("I'm up!");
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void requestVersion() {

        javax.ws.rs.core.Response response = client.target(SERVER_URI)
                .path("/version")
                .request()
                .get();

        assertThat(response).isNotNull();
        assertThat(response.readEntity(String.class))
                .isEqualTo("No version defined yet!");
        assertThat(response.getStatus()).isEqualTo(200);

    }

    @Test
    public void requestOpenApiSchema() throws IOException {

        javax.ws.rs.core.Response response = client
                .target(SERVER_URI)
                .path("/api")
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertThat(response).isNotNull();
        String body = response.readEntity(String.class);
        LOGGER.debug("Schema {}", body);

        assertThat(body).isNotEmpty();
        assertThat(response.getMediaType())
                .isEqualTo(MediaType.APPLICATION_JSON_TYPE);
        assertThat(response.getStatus()).isEqualTo(200);

        try (Reader reader = new StringReader(body)) {
            JsonObject result = Json.createReader(reader).readObject();

            assertThat(result).isNotEmpty();

        }

    }

    @Test
    public void requestOpenApiSchemaDocument() {

        javax.ws.rs.core.Response response = client
                .target(SERVER_URI)
                .path("/api")
                .request(MediaType.TEXT_HTML)
                .get();

        assertThat(response).isNotNull();
        String body = response.readEntity(String.class);
        LOGGER.debug("Doc {}", body);
        assertThat(body).isNotEmpty();
        assertThat(response.getMediaType())
                .isEqualTo(MediaType.TEXT_HTML_TYPE);
        assertThat(response.getStatus()).isEqualTo(200);

    }

}
