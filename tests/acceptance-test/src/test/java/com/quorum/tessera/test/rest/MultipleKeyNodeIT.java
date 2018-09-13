package com.quorum.tessera.test.rest;

import org.junit.Test;

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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This tests that a node that hosts multiple sets of keys can send/receive
 * transactions for both keys
 */
public class MultipleKeyNodeIT {

    private static final URI SEND_SERVER_URI = UriBuilder.fromUri("http://127.0.0.1").port(8080).build();

    private static final URI SERVER_URI = UriBuilder.fromUri("http://127.0.0.1").port(8082).build();

    private final Client client = ClientBuilder.newClient();

    @Test
    public void storePayloadOnFirstKey() throws UnsupportedEncodingException {

        final String recipientPublicKey = "giizjhZQM6peq52O7icVFxdTmTYinQSUsvyhXzgZqkE=";

        final String txHash = this.sendNewPayload(recipientPublicKey);

        //retrieve the transaction
        final Response retrieveResponse = this.client.target(SERVER_URI)
            .path("/transaction/" + URLEncoder.encode(txHash, "UTF-8"))
            .request()
            .buildGet()
            .invoke();

        assertThat(retrieveResponse).isNotNull();
        assertThat(retrieveResponse.getStatus()).isEqualTo(200);

        final String result = retrieveResponse.readEntity(String.class);
        final Reader reader = new StringReader(result);
        final JsonObject jsonResult = Json.createReader(reader).readObject();
        assertThat(jsonResult).containsKeys("payload");
        assertThat(jsonResult.getString("payload")).isEqualTo("Zm9v");

    }

    @Test
    public void storePayloadOnSecondKey() throws UnsupportedEncodingException {

        final String recipientPublicKey = "jP4f+k/IbJvGyh0LklWoea2jQfmLwV53m9XoHVS4NSU=";

        final String txHash = this.sendNewPayload(recipientPublicKey);

        //retrieve the transaction
        final Response retrieveResponse = this.client.target(SERVER_URI)
            .path("/transaction/" + URLEncoder.encode(txHash, "UTF-8"))
            .request()
            .buildGet()
            .invoke();

        assertThat(retrieveResponse).isNotNull();
        assertThat(retrieveResponse.getStatus()).isEqualTo(200);

        final String result = retrieveResponse.readEntity(String.class);
        final Reader reader = new StringReader(result);
        final JsonObject jsonResult = Json.createReader(reader).readObject();
        assertThat(jsonResult).containsKeys("payload");
        assertThat(jsonResult.getString("payload")).isEqualTo("Zm9v");

    }

    private String sendNewPayload(final String recipientPublicKey) {

        final String sendRequest = Json.createObjectBuilder()
            .add("from", "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=")
            .add("to", Json.createArrayBuilder().add(recipientPublicKey))
            .add("payload", "Zm9v").build().toString();

        final Response response = this.client.target(SEND_SERVER_URI)
            .path("/send")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        //validate result

        final String result = response.readEntity(String.class);
        final Reader reader = new StringReader(result);
        final JsonObject jsonResult = Json.createReader(reader).readObject();
        assertThat(jsonResult).containsKeys("key");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        return jsonResult.getString("key");
    }

}
