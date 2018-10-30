package com.quorum.tessera.test.rest;

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
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class DeleteIT {

    private static final URI SERVER_URI = NODE1_Q2T_URI;

    private static final Client client = ClientBuilder.newClient();

    @Test
    public void deleteTransactionThatExists() throws UnsupportedEncodingException {
        //setup (sending in a tx)
        final String hash = client.target(SERVER_URI)
                .path("/sendraw")
                .request()
                .header("c11n-from", PTY1_KEY)
                .header("c11n-to", PTY2_KEY)
                .post(Entity.entity(TXN_DATA, MediaType.APPLICATION_OCTET_STREAM))
                .readEntity(String.class);

        final String encodedHash = URLEncoder.encode(hash, UTF_8.toString());

        //delete it
        final Response resp = client.target(SERVER_URI)
                .path("transaction")
                .path(encodedHash)
                .request()
                .delete();

        //validate result
        assertThat(resp).isNotNull();
        assertThat(resp.getStatus()).isEqualTo(204);

    }

    @Test
    public void deleteTransactionThatDoesntExist() throws Exception {

        final String madeupHash = Base64.getUrlEncoder().encodeToString("madeup".getBytes());

        final Response response = client.target(SERVER_URI)
                .path("transaction")
                .path(madeupHash)
                .request()
                .buildDelete()
                .invoke();

        //validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(404);

    }

}
