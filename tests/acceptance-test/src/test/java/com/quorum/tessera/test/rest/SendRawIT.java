package com.quorum.tessera.test.rest;

import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class SendRawIT {

    private static final URI SERVER_URI = UriBuilder.fromUri("http://127.0.0.1").port(8080).build();

    private static final String SEND_PATH = "/sendraw";

    private static final String SENDER = "c11n-from";

    private static final String RECIPIENTS = "c11n-to";

    private static final String SENDER_KEY = "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=";

    private static final String RECIPIENT_ONE = "yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=";

    private static final String RECIPIENT_TWO = "giizjhZQM6peq52O7icVFxdTmTYinQSUsvyhXzgZqkE=";

    private final Client client = ClientBuilder.newClient();

    /**
     * Quorum sends transaction with singe public recipient key
     */
    @Test
    public void sendToSingleRecipient() {

        final Response response = client.target(SERVER_URI)
            .path(SEND_PATH)
            .request()
            .header(SENDER, SENDER_KEY)
            .header(RECIPIENTS, RECIPIENT_ONE)
            .post(Entity.entity("Zm9v".getBytes(), MediaType.APPLICATION_OCTET_STREAM));

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isNotNull();

    }

    /**
     * Quorum sends transaction with multiple public recipient keys
     */
    @Test
    public void sendSingleTransactionToMultipleParties() {

        final Response response = client.target(SERVER_URI)
            .path(SEND_PATH)
            .request()
            .header(SENDER, SENDER_KEY)
            .header(RECIPIENTS, RECIPIENT_ONE + "," + RECIPIENT_TWO)
            .post(Entity.entity("Zm9v".getBytes(), MediaType.APPLICATION_OCTET_STREAM));

        //validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isNotNull();
    }

    @Test
    public void sendTransactionWithoutASender() {

        final Response response = client.target(SERVER_URI)
            .path(SEND_PATH)
            .request()
            .header(RECIPIENTS, RECIPIENT_ONE)
            .post(Entity.entity("Zm9v".getBytes(), MediaType.APPLICATION_OCTET_STREAM));

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isNotNull();

    }

    @Test
    public void sendTransactionWithEmptyRecipients() {

        final Response response = client.target(SERVER_URI)
            .path(SEND_PATH)
            .request()
            .header(SENDER, SENDER_KEY)
            .header(RECIPIENTS, "")
            .post(Entity.entity("Zm9v".getBytes(), MediaType.APPLICATION_OCTET_STREAM));

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isNotNull();
    }

    @Test
    public void sendTransactionWithMissingRecipients() {

        final Response response = client.target(SERVER_URI)
            .path(SEND_PATH)
            .request()
            .header(SENDER, SENDER_KEY)
            .post(Entity.entity("Zm9v".getBytes(), MediaType.APPLICATION_OCTET_STREAM));

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isNotNull();

    }

    @Test
    public void missingPayloadFails() {

        final Response response = client.target(SERVER_URI)
            .path(SEND_PATH)
            .request()
            .header(SENDER, SENDER_KEY)
            .header(RECIPIENTS, RECIPIENT_ONE)
            .post(Entity.entity(null, MediaType.APPLICATION_OCTET_STREAM));

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(400);
    }

    /**
     * Quorum sends transaction with unknown public key
     */
    @Test
    public void sendUnknownPublicKey() {

        final Response response = client.target(SERVER_URI)
            .path(SEND_PATH)
            .request()
            .header(SENDER, SENDER_KEY)
            .header(RECIPIENTS, "8SjRHlUBe4hAmTk3KDeJ96RhN+s10xRrHDrxEi1O5W0=")
            .post(Entity.entity("Zm9v".getBytes(), MediaType.APPLICATION_OCTET_STREAM));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(400);
    }

}
