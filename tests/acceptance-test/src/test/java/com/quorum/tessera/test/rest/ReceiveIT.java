package com.quorum.tessera.test.rest;

import org.junit.Before;
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * retrieve tx with hash
 * retrieve tx with hash and sender
 * retrieve hash that doesn't exist
 */
public class ReceiveIT {

    private static final URI SERVER_URI = UriBuilder.fromUri("http://127.0.0.1").port(8080).build();

    private static final String RECEIVE_PATH = "/transaction";

    private static final String SENDER_KEY = "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=";

    private static final String RECIPIENT_ONE = "yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=";

    private static final String PAYLOAD = "Zm9v";

    private static final Client client = ClientBuilder.newClient();

    private String encodedHash;

    private String encodedSender;

    private String encodedRecipientOne;

    //Persist a single transaction that can be used later
    @Before
    public void init() throws UnsupportedEncodingException {

        final String sendRequest = Json.createObjectBuilder()
            .add("from", SENDER_KEY)
            .add("to", Json.createArrayBuilder().add(RECIPIENT_ONE))
            .add("payload", PAYLOAD).build().toString();

        final Response response = client.target(SERVER_URI)
            .path("/send")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        final String result = response.readEntity(String.class);
        final Reader reader = new StringReader(result);

        final JsonObject jsonResult = Json.createReader(reader).readObject();

        final String hash = jsonResult.getString("key");

        this.encodedHash = URLEncoder.encode(hash, UTF_8.toString());
        this.encodedSender = URLEncoder.encode(SENDER_KEY, UTF_8.toString());
        this.encodedRecipientOne = URLEncoder.encode(RECIPIENT_ONE, UTF_8.toString());

    }

    @Test
    public void fetchExistingTransactionUsingOwnKey() {

        final Response response = client.target(SERVER_URI)
            .path(RECEIVE_PATH + "/" + this.encodedHash)
            .request()
            .buildGet()
            .property("to", this.encodedSender)
            .invoke();

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final String result = response.readEntity(String.class);
        final Reader reader = new StringReader(result);
        final JsonObject jsonResult = Json.createReader(reader).readObject();
        assertThat(jsonResult).containsKeys("payload");
        assertThat(jsonResult.getString("payload")).isEqualTo(PAYLOAD);

    }

    @Test
    public void fetchExistingTransactionUsingRecipientKey() {

        final Response response = client.target(SERVER_URI)
            .path(RECEIVE_PATH + "/" + this.encodedHash)
            .request()
            .buildGet()
            .property("to", this.encodedRecipientOne)
            .invoke();

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final String result = response.readEntity(String.class);
        final Reader reader = new StringReader(result);
        final JsonObject jsonResult = Json.createReader(reader).readObject();
        assertThat(jsonResult).containsKeys("payload");
        assertThat(jsonResult.getString("payload")).isEqualTo(PAYLOAD);

    }

    @Test
    public void fetchExistingTransactionNotUsingKey() {

        final Response response = client.target(SERVER_URI)
            .path(RECEIVE_PATH + "/" + this.encodedHash)
            .request()
            .buildGet()
            .invoke();

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final String result = response.readEntity(String.class);
        final Reader reader = new StringReader(result);
        final JsonObject jsonResult = Json.createReader(reader).readObject();
        assertThat(jsonResult).containsKeys("payload");
        assertThat(jsonResult.getString("payload")).isEqualTo(PAYLOAD);

    }

    @Test
    public void fetchNonexistantTransactionFails() {

        final Response response = client.target(SERVER_URI)
            .path(RECEIVE_PATH + "/" + "invalidhashvalue")
            .request()
            .buildGet()
            .invoke();

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(404);

        final String result = response.readEntity(String.class);
        assertThat(result).isEqualTo("Message with hash invalidhashvalue was not found");

    }

}
