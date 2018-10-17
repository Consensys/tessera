package com.quorum.tessera.test.rest;

import com.quorum.tessera.api.model.ReceiveResponse;
import com.quorum.tessera.api.model.SendRequest;
import com.quorum.tessera.api.model.SendResponse;
import static com.quorum.tessera.test.Fixtures.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Base64;
import javax.json.Json;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Scenarios tested:
 * <p>
 * - 1 sender, 1 private for - 1 sender, 2 private for - TODO: 1 sender, 2
 * private for, 1 is down - 0 sender, 1 private for - 1 sender, 0 private for -
 * no payload - sending when it isn't json - sending to an unknown recipient -
 * TODO: send using an unknown sender key
 */
public class SendIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendIT.class);

    public static final URI SERVER_URI = NODE1_URI;

    private static final String SEND_PATH = "/send";

    private static final byte[] TXN_DATA = "Zm9v".getBytes();

    private static final byte[] TXN_DATA_BASE64 = Base64.getEncoder().encode(TXN_DATA);

    private final Client client = ClientBuilder.newClient();


    /**
     * Quorum sends transaction with single public recipient key
     */
    @Test
    public void sendToSingleRecipient() {

        final SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(PTY1_KEY);
        sendRequest.setTo(PTY2_KEY);
        sendRequest.setPayload(TXN_DATA);

        LOGGER.info("sendRequest: {}", sendRequest);

        final Response response = client.target(SERVER_URI)
                .path(SEND_PATH)
                .request()
                .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        //validate result
        final SendResponse result = response.readEntity(SendResponse.class);
        assertThat(result.getKey()).isNotNull().isNotBlank();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);

        URI location = response.getLocation();

        final Response checkPersistedTxnResponse = client.target(location)
                .request()
                .get();

        assertThat(checkPersistedTxnResponse.getStatus()).isEqualTo(200);

        ReceiveResponse receiveResponse = checkPersistedTxnResponse.readEntity(ReceiveResponse.class);

        assertThat(receiveResponse.getPayload()).isEqualTo(TXN_DATA);

    }

    /**
     * Quorum sends transaction with multiple public recipient keys
     */
    @Test
    public void sendSingleTransactionToMultipleParties() {

        final SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(PTY1_KEY);
        sendRequest.setTo(PTY2_KEY, PTY3_KEY);
        sendRequest.setPayload(TXN_DATA);

        LOGGER.info("sendRequest: {}", sendRequest);

        final Response response = client.target(SERVER_URI)
                .path(SEND_PATH)
                .request()
                .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        final SendResponse result = response.readEntity(SendResponse.class);
        assertThat(result.getKey()).isNotNull().isNotBlank();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);

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

        final SendRequest sendRequest = new SendRequest();
        sendRequest.setTo(PTY2_KEY);
        sendRequest.setPayload(TXN_DATA);

        LOGGER.info("sendRequest: {}", sendRequest);

        final Response response = client.target(SERVER_URI)
                .path(SEND_PATH)
                .request()
                .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        final SendResponse result = response.readEntity(SendResponse.class);
        assertThat(result.getKey()).isNotNull().isNotBlank();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);

        URI location = response.getLocation();

        final Response checkPersistedTxnResponse = client.target(location)
                .request()
                .get();

        assertThat(checkPersistedTxnResponse.getStatus()).isEqualTo(200);

        ReceiveResponse receiveResponse = checkPersistedTxnResponse.readEntity(ReceiveResponse.class);

        assertThat(receiveResponse.getPayload()).isEqualTo(TXN_DATA);
    }

    @Test
    public void sendTransactionWithMissingRecipients() {

        final SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(PTY1_KEY);
        sendRequest.setPayload(TXN_DATA);

        LOGGER.info("sendRequest: {}", sendRequest);

        final Response response = client.target(SERVER_URI)
                .path(SEND_PATH)
                .request()
                .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        final SendResponse result = response.readEntity(SendResponse.class);
        assertThat(result.getKey()).isNotNull().isNotBlank();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);

        URI location = response.getLocation();

        final Response checkPersistedTxnResponse = client.target(location)
                .request()
                .get();

        assertThat(checkPersistedTxnResponse.getStatus()).isEqualTo(200);

        ReceiveResponse receiveResponse = checkPersistedTxnResponse.readEntity(ReceiveResponse.class);

        assertThat(receiveResponse.getPayload()).isEqualTo(TXN_DATA);

    }

    @Test
    public void missingPayloadFails() {

        final String sendRequest = Json.createObjectBuilder()
                .add("from", PTY1_KEY)
                .add("to",
                        Json.createArrayBuilder().add(PTY2_KEY)
                )
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

    @Test
    public void emptyMessageFails() {

        final String sendRequest = "{}";

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

        final SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(PTY1_KEY);
        sendRequest.setTo("8SjRHlUBe4hAmTk3KDeJ96RhN+s10xRrHDrxEi1O5W0=");
        sendRequest.setPayload(TXN_DATA_BASE64);

        LOGGER.info("sendRequest: {}", sendRequest);

        final Response response = client.target(SERVER_URI)
                .path(SEND_PATH)
                .request()
                .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(400);
    }

}
