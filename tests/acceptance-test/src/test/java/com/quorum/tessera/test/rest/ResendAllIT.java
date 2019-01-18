package com.quorum.tessera.test.rest;

import com.quorum.tessera.api.model.ResendRequest;
import com.quorum.tessera.api.model.ResendRequestType;
import static com.quorum.tessera.test.Fixtures.*;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class ResendAllIT {

    private static final URI PRIMARY_SERVER_Q2T_URI = NODE1_Q2T_URI;
    private static final URI PRIMARY_SERVER_P2P_URI = NODE1_P2P_URI;

    private static final URI SECONDARY_SERVER_Q2T_URI = NODE2_Q2T_URI;
    private static final URI SECONDARY_SERVER_P2P_URI = NODE2_P2P_URI;

    private final Client client = ClientBuilder.newClient();

    private static final String RESEND_PATH = "/resend";


    @Test
    public void resendTransactionsForGivenKey() throws UnsupportedEncodingException {
        //setup (sending in a tx)

        Response sendRawResponse = client.target(PRIMARY_SERVER_Q2T_URI)
                .path("/sendraw")
                .request()
                .header("c11n-from", PTY1_KEY)
                .header("c11n-to", PTY2_KEY)
                .post(Entity.entity(TXN_DATA, MediaType.APPLICATION_OCTET_STREAM));

        URI location = sendRawResponse.getLocation();

        String hash = sendRawResponse.readEntity(String.class);

        final String encodedHash = URLEncoder.encode(hash, UTF_8.toString());

        //delete it from the recipient node
        final Response deleteReq = client.target(location)
                .request()
                .delete();

        assertThat(deleteReq).isNotNull();
        assertThat(deleteReq.getStatus()).isEqualTo(204);

        //check it is deleted
        final Response deleteCheck = client.target(PRIMARY_SERVER_Q2T_URI)
                .path("transaction")
                .path(encodedHash)
                .request()
                .get();

        assertThat(deleteCheck).isNotNull();
        assertThat(deleteCheck.getStatus()).isEqualTo(404);

        //perform resend
        final ResendRequest req = new ResendRequest();
        req.setType(ResendRequestType.ALL);
        req.setPublicKey(PTY2_KEY);

        final Response resendRequest = client.target(PRIMARY_SERVER_P2P_URI)
                .path(RESEND_PATH)
                .request()
                .buildPost(Entity.entity(req, MediaType.APPLICATION_JSON_TYPE))
                .invoke();

        assertThat(resendRequest).isNotNull();
        assertThat(resendRequest.getStatus()).isEqualTo(200);

        //and fetch the transaction to make sure it is there
        final Response resendCheck = client.target(SECONDARY_SERVER_Q2T_URI)
                .path("transaction")
                .path(encodedHash)
                .request()
                .get();

        assertThat(resendCheck).isNotNull();
        assertThat(resendCheck.getStatus()).isEqualTo(200);

    }

    @Test
    public void resendForKeyWithNoTransactions() {
        //perform resend
        final ResendRequest req = new ResendRequest();
        req.setType(ResendRequestType.ALL);
        req.setPublicKey("rUSW9gnm2Unm5ECvEfuU10LX7KYsN59Flw7m7iu6wEo=");

        final Response resendRequest = client.target(PRIMARY_SERVER_P2P_URI)
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

        final Response resendRequest = client.target(PRIMARY_SERVER_P2P_URI)
                .path(RESEND_PATH)
                .request()
                .buildPost(Entity.entity(req, MediaType.APPLICATION_JSON_TYPE))
                .invoke();

        assertThat(resendRequest).isNotNull();
        assertThat(resendRequest.getStatus()).isEqualTo(400);
    }

}
