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
import java.net.URI;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

public class ReceiveRawIT {

    private static final URI SERVER_URI = UriBuilder.fromUri("http://127.0.0.1").port(8080).build();

    private static final URI SECONDAERY_SERVER = UriBuilder.fromUri("http://127.0.0.1").port(8081).build();

    private static final String RECEIVE_PATH = "/receiveraw";

    private static final String C11N_TO = "c11n-to";

    private static final String C11N_KEY = "c11n-key";

    private static final String SENDER_KEY = "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=";

    private static final String RECIPIENT_ONE = "yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=";

    private static final String PAYLOAD = "Zm9v";

    private static final String RAW_PAYLOAD = new String(Base64.getDecoder().decode(PAYLOAD));

    private static final Client client = ClientBuilder.newClient();

    private String hash;

    //Persist a single transaction that can be used later
    @Before
    public void init() {
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

        this.hash = jsonResult.getString("key");

    }

    @Test
    public void fetchExistingTransactionUsingOwnKey() {

        final Response response = client.target(SERVER_URI)
            .path(RECEIVE_PATH)
            .request()
            .header(C11N_KEY, this.hash)
            .header(C11N_TO, SENDER_KEY)
            .buildGet()
            .invoke();

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final byte[] result = response.readEntity(byte[].class);

        assertThat(new String(result)).isEqualTo(RAW_PAYLOAD);

    }

    @Test
    public void fetchExistingTransactionNotUsingKeyOnSender() {

        final Response response = client.target(SERVER_URI)
            .path(RECEIVE_PATH)
            .request()
            .header(C11N_KEY, this.hash)
            .buildGet()
            .invoke();

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final byte[] result = response.readEntity(byte[].class);

        assertThat(new String(result)).isEqualTo(RAW_PAYLOAD);

    }

    @Test
    public void fetchExistingTransactionNotUsingKeyOnRecipient() {

        final Response response = client.target(SECONDAERY_SERVER)
            .path(RECEIVE_PATH)
            .request()
            .header(C11N_KEY, this.hash)
            .buildGet()
            .invoke();

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final byte[] result = response.readEntity(byte[].class);

        assertThat(new String(result)).isEqualTo(RAW_PAYLOAD);

    }

    @Test
    public void fetchExistingTransactionUsingRecipientKey() {

        final Response response = client.target(SECONDAERY_SERVER)
            .path(RECEIVE_PATH)
            .request()
            .header(C11N_KEY, this.hash)
            .header(C11N_TO, RECIPIENT_ONE)
            .buildGet()
            .invoke();

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final byte[] result = response.readEntity(byte[].class);

        assertThat(new String(result)).isEqualTo(RAW_PAYLOAD);

    }

    @Test
    public void fetchNonexistantTransactionFails() {

        final Response response = client.target(SERVER_URI)
            .path(RECEIVE_PATH)
            .request()
            .header(C11N_KEY, "invalidhashvalue")
            .buildGet()
            .invoke();

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(404);

        final String result = response.readEntity(String.class);
        assertThat(result).isEqualTo("Message with hash invalidhashvalue was not found");

    }

}
