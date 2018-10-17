package com.quorum.tessera.test.rest;

import com.quorum.tessera.api.model.ReceiveResponse;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.UUID;

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

    private static final byte[] TXN_DATA = "Zm9v".getBytes();

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
                .post(Entity.entity(TXN_DATA, MediaType.APPLICATION_OCTET_STREAM));

        //validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        String persistedKey = response.readEntity(String.class);
        assertThat(persistedKey).isNotNull();

        URI location = response.getLocation();

        final Response checkPersistedTxnResponse = client.target(location)
                .request()
                .get();

        assertThat(checkPersistedTxnResponse.getStatus())
                .isEqualTo(200);

        ReceiveResponse receiveResponse
                = checkPersistedTxnResponse.readEntity(ReceiveResponse.class);

        assertThat(receiveResponse.getPayload()).isEqualTo(TXN_DATA);
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
                .post(Entity.entity(TXN_DATA, MediaType.APPLICATION_OCTET_STREAM));

        //validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        String persistedKey = response.readEntity(String.class);
        assertThat(persistedKey).isNotNull();

        URI location = response.getLocation();

        final Response checkPersistedTxnResponse = client.target(location)
                .request()
                .get();

        assertThat(checkPersistedTxnResponse.getStatus()).isEqualTo(200);

        ReceiveResponse receiveResponse = checkPersistedTxnResponse.readEntity(ReceiveResponse.class);

        assertThat(receiveResponse.getPayload()).isEqualTo(TXN_DATA);

    }

    @Test
    public void sendTransactionWithoutASender() {

        final Response response = client.target(SERVER_URI)
                .path(SEND_PATH)
                .request()
                .header(RECIPIENTS, RECIPIENT_ONE)
                .post(Entity.entity(TXN_DATA, MediaType.APPLICATION_OCTET_STREAM));

        //validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        URI location = response.getLocation();

        final Response checkPersistedTxnResponse = client.target(location)
                .request()
                .get();

        assertThat(checkPersistedTxnResponse.getStatus()).isEqualTo(200);

        ReceiveResponse receiveResponse = checkPersistedTxnResponse.readEntity(ReceiveResponse.class);

        assertThat(receiveResponse.getPayload()).isEqualTo(TXN_DATA);

    }

    @Test
    public void sendTransactionWithEmptyRecipients() {

        byte[] txnData = UUID.randomUUID().toString().getBytes();
        
        final Response response = client.target(SERVER_URI)
                .path(SEND_PATH)
                .request()
                .header(SENDER, SENDER_KEY)
                .header(RECIPIENTS, "")
                .post(Entity.entity(txnData, MediaType.APPLICATION_OCTET_STREAM));

        //validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        String persistedKey = response.readEntity(String.class);
        assertThat(persistedKey).isNotNull();

        URI location = response.getLocation();

        final Response checkPersistedTxnResponse = client.target(location)
                .request()
                .get();

        assertThat(checkPersistedTxnResponse.getStatus()).isEqualTo(200);

        ReceiveResponse receiveResponse = checkPersistedTxnResponse.readEntity(ReceiveResponse.class);

        assertThat(receiveResponse.getPayload()).isEqualTo(txnData);
    }

    /**
     * In the case where no recipients are defined its is assumed that
     */
    @Test
    public void sendTransactionWithMissingRecipients() {

        byte[] txnData = UUID.randomUUID().toString().getBytes();
        
        final Response response = client.target(SERVER_URI)
                .path(SEND_PATH)
                .request()
                .header(SENDER, SENDER_KEY)
                .post(Entity.entity(txnData, MediaType.APPLICATION_OCTET_STREAM));

        //validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        String persistedKey = response.readEntity(String.class);
        assertThat(persistedKey).isNotNull();

        URI location = response.getLocation();

        final Response checkPersistedTxnResponse = client.target(location)
                .request()
                .get();

        assertThat(checkPersistedTxnResponse.getStatus()).isEqualTo(200);

        ReceiveResponse receiveResponse = checkPersistedTxnResponse.readEntity(ReceiveResponse.class);

        assertThat(receiveResponse.getPayload()).isEqualTo(txnData);
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
                .post(Entity.entity(TXN_DATA, MediaType.APPLICATION_OCTET_STREAM));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(400);
    }

}
