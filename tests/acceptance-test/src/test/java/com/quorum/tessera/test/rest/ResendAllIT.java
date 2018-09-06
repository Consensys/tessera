package com.quorum.tessera.test.rest;

import com.quorum.tessera.api.model.ResendRequest;
import com.quorum.tessera.api.model.ResendRequestType;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class ResendAllIT {

    private static final URI PRIMARY_SERVER_URI = UriBuilder.fromUri("http://127.0.0.1").port(8080).build();

    private static final URI SECONDARY_SERVER_URI = UriBuilder.fromUri("http://127.0.0.1").port(8081).build();

    private static final Client client = ClientBuilder.newClient();

    private static final String RESEND_PATH = "/resend";

    private static final String RECIPIENT_KEY = "yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=";

    @Test
    public void resendTransactionsForGivenKey() throws UnsupportedEncodingException {
        //setup (sending in a tx)
        final String hash = client.target(PRIMARY_SERVER_URI)
            .path("/sendraw")
            .request()
            .header("c11n-from", "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=")
            .header("c11n-to", RECIPIENT_KEY)
            .post(Entity.entity("Zm9v".getBytes(), MediaType.APPLICATION_OCTET_STREAM))
            .readEntity(String.class);

        final String encodedHash = URLEncoder.encode(hash, UTF_8.toString());

        //delete it from the recipient node

        final Response deleteReq = client.target(SECONDARY_SERVER_URI)
            .path("/transaction/" + encodedHash)
            .request()
            .buildDelete()
            .invoke();

        assertThat(deleteReq).isNotNull();
        assertThat(deleteReq.getStatus()).isEqualTo(204);

        //check it is deleted

        final Response deleteCheck = client.target(SECONDARY_SERVER_URI)
            .path("/transaction/" + encodedHash)
            .request()
            .buildGet()
            .invoke();

        assertThat(deleteCheck).isNotNull();
        assertThat(deleteCheck.getStatus()).isEqualTo(404);

        //perform resend
        final ResendRequest req = new ResendRequest();
        req.setType(ResendRequestType.ALL);
        req.setPublicKey(RECIPIENT_KEY);

        final Response resendRequest = client.target(PRIMARY_SERVER_URI)
            .path(RESEND_PATH)
            .request()
            .buildPost(Entity.entity(req, MediaType.APPLICATION_JSON_TYPE))
            .invoke();

        assertThat(resendRequest).isNotNull();
        assertThat(resendRequest.getStatus()).isEqualTo(200);

        //and fetch the transaction to make sure it is there
        final Response resendCheck = client.target(SECONDARY_SERVER_URI)
            .path("/transaction/" + encodedHash)
            .request()
            .buildGet()
            .invoke();

        assertThat(resendCheck).isNotNull();
        assertThat(resendCheck.getStatus()).isEqualTo(200);

    }

    @Test
    public void resendForKeyWithNoTransactions() {
        //perform resend
        final ResendRequest req = new ResendRequest();
        req.setType(ResendRequestType.ALL);
        req.setPublicKey("rUSW9gnm2Unm5ECvEfuU10LX7KYsN59Flw7m7iu6wEo=");

        final Response resendRequest = client.target(PRIMARY_SERVER_URI)
            .path(RESEND_PATH)
            .request()
            .buildPost(Entity.entity(req, MediaType.APPLICATION_JSON_TYPE))
            .invoke();

        assertThat(resendRequest).isNotNull();
        assertThat(resendRequest.getStatus()).isEqualTo(200);
    }

    @Test
    public void resendForInvalidKeyErrors() {
        //perform resend
        final ResendRequest req = new ResendRequest();
        req.setType(ResendRequestType.ALL);
        req.setPublicKey("rUSW9gnm2Unm5ECvEfuU&&&&&&&&59Flw7m7iu6wEo=");

        final Response resendRequest = client.target(PRIMARY_SERVER_URI)
            .path(RESEND_PATH)
            .request()
            .buildPost(Entity.entity(req, MediaType.APPLICATION_JSON_TYPE))
            .invoke();

        assertThat(resendRequest).isNotNull();
        assertThat(resendRequest.getStatus()).isEqualTo(400);
    }

}
