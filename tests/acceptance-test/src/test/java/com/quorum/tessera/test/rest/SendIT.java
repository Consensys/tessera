package com.quorum.tessera.test.rest;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Scenarios tested:
 * <p>
 * - 1 sender, 1 private for
 * - 1 sender, 2 private for
 * - TODO: 1 sender, 2 private for, 1 is down
 * - 0 sender, 1 private for
 * - 1 sender, 0 private for
 * - no payload
 * - sending when it isn't json
 * - sending to an unknown recipient
 * - TODO: send using an unknown sender key
 */
public class SendIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendIT.class);

    public static final URI SERVER_URI = UriBuilder.fromUri("http://127.0.0.1").port(8080).build();

    private static final String SEND_PATH = "/send";

    private final Client client = ClientBuilder.newClient();

    /**
     * Quorum sends transaction with single public recipient key
     */
    @Test
    public void sendToSingleRecipient() {

        final String sendRequest = Json.createObjectBuilder()
            .add("from", "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=")
            .add("to", Json.createArrayBuilder().add("yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0="))
            .add("payload", "Zm9v").build().toString();

        LOGGER.info("sendRequest: {}", sendRequest);

        final Response response = client.target(SERVER_URI)
            .path(SEND_PATH)
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        //validate result

        final String result = response.readEntity(String.class);
        final Reader reader = new StringReader(result);
        final JsonObject jsonResult = Json.createReader(reader).readObject();
        assertThat(jsonResult).containsKeys("key");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

    }

    /**
     * Quorum sends transaction with multiple public recipient keys
     */
    @Test
    public void sendSingleTransactionToMultipleParties() {
        final String sendRequest = Json.createObjectBuilder()
            .add("from", "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=")
            .add("to", Json.createArrayBuilder()
                .add("yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=")
                .add("giizjhZQM6peq52O7icVFxdTmTYinQSUsvyhXzgZqkE=")
            )
            .add("payload", "Zm9v").build().toString();

        LOGGER.info("sendRequest: {}", sendRequest);

        final Response response = client.target(SERVER_URI)
            .path(SEND_PATH)
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final String result = response.readEntity(String.class);
        final Reader reader = new StringReader(result);
        final JsonObject jsonResult = Json.createReader(reader).readObject();
        assertThat(jsonResult).containsKeys("key");
    }

    @Test
    public void sendTransactionWithoutASender() {

        final String sendRequest = Json.createObjectBuilder()
            .add("to", Json.createArrayBuilder().add("yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0="))
            .add("payload", "Zm9v").build().toString();

        LOGGER.info("sendRequest: {}", sendRequest);

        final Response response = client.target(SERVER_URI)
            .path(SEND_PATH)
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final String result = response.readEntity(String.class);
        LOGGER.debug(result);

        final Reader reader = new StringReader(result);
        final JsonObject jsonResult = Json.createReader(reader).readObject();
        assertThat(jsonResult).containsKeys("key");

//        final String b64Hash = jsonResult.getString("key");
        //TODO: add retrieving the transaction and checking if the default sender was added

    }

    @Test
    public void sendTransactionWithEmptyRecipients() {

        final String sendRequest = Json.createObjectBuilder()
            .add("from", "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=")
            .add("to", Json.createArrayBuilder())
            .add("payload", "Zm9v").build().toString();

        LOGGER.info("sendRequest: {}", sendRequest);

        final Response response = client.target(SERVER_URI)
            .path(SEND_PATH)
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final String result = response.readEntity(String.class);
        LOGGER.debug(result);

        final Reader reader = new StringReader(result);
        final JsonObject jsonResult = Json.createReader(reader).readObject();
        assertThat(jsonResult).containsKeys("key");
    }

    @Test
    public void sendTransactionWithMissingRecipients() {

        final String sendRequest = Json.createObjectBuilder()
            .add("from", "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=")
            .add("payload", "Zm9v").build().toString();

        LOGGER.info("sendRequest: {}", sendRequest);

        final Response response = client.target(SERVER_URI)
            .path(SEND_PATH)
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final String result = response.readEntity(String.class);
        LOGGER.debug(result);

        final Reader reader = new StringReader(result);
        final JsonObject jsonResult = Json.createReader(reader).readObject();
        assertThat(jsonResult).containsKeys("key");

    }

    @Test
    public void missingPayloadFails() {

        final String sendRequest = Json.createObjectBuilder()
            .add("from", "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=")
            .add("to", Json.createArrayBuilder().add("yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0="))
            .build().toString();

        LOGGER.info("sendRequest: {}", sendRequest);

        final Response response = client.target(SERVER_URI)
            .path(SEND_PATH)
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void garbageMessageFails() {

        final String sendRequest = "this is clearly a garbage message";

        LOGGER.info("sendRequest: {}", sendRequest);

        final Response response = client.target(SERVER_URI)
            .path(SEND_PATH)
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(400);
    }

    /**
     * Quorum sends transaction with unknown public key
     */
    @Test
    public void sendUnknownPublicKey() {

        final String sendRequest = Json.createObjectBuilder()
            .add("from", "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=")
            .add("to", Json.createArrayBuilder().add("8SjRHlUBe4hAmTk3KDeJ96RhN+s10xRrHDrxEi1O5W0="))
            .add("payload", "Zm9v").build().toString();

        LOGGER.info("sendRequest: {}", sendRequest);

        final Response response = client.target(SERVER_URI)
            .path(SEND_PATH)
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(400);
    }

    /**
     * Quorum sends transaction with unknown public key
     */
   // @Test
    public void sendToDeadNode() {

        final String sendRequest = Json.createObjectBuilder()
            .add("from", "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=")
            .add("to", Json.createArrayBuilder().add("Tj8xg/HpsYmh7Te3UerzlLx1HgpWVOGq25ZgbwaPNVM="))
            .add("payload", "Zm9v").build().toString();

        LOGGER.info("sendRequest: {}", sendRequest);

        final Response response = client.target(SERVER_URI)
            .path(SEND_PATH)
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(500);
    }

}
