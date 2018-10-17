package com.quorum.tessera.test.rest;

import com.quorum.tessera.api.model.SendRequest;
import static com.quorum.tessera.test.Fixtures.*;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import static org.assertj.core.api.Assertions.assertThat;

public class ReceiveRawIT {

    private static final URI SERVER_URI = NODE1_URI;

    private static final String RECEIVE_PATH = "/receiveraw";

    private static final String C11N_TO = "c11n-to";

    private static final String C11N_KEY = "c11n-key";

    private static final byte[] PAYLOAD = TXN_DATA;
    

    private Client client = ClientBuilder.newClient();

    private String hash;

    //Persist a single transaction that can be used later
    @Before
    public void init() {
        
        SendRequest sendRequest = new SendRequest();
        sendRequest.setPayload(PAYLOAD);
        sendRequest.setTo(PTY2_KEY);
        sendRequest.setFrom(PTY1_KEY);

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
            .header(C11N_TO, PTY1_KEY)
            .buildGet()
            .invoke();

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final byte[] result = response.readEntity(byte[].class);

        assertThat(result).isEqualTo(PAYLOAD);

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

        assertThat(result).isEqualTo(PAYLOAD);

    }

    @Test
    public void fetchExistingTransactionNotUsingKeyOnRecipient() {

        final Response response = client.target(NODE2_URI)
            .path(RECEIVE_PATH)
            .request()
            .header(C11N_KEY, this.hash)
            .buildGet()
            .invoke();

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final byte[] result = response.readEntity(byte[].class);

        assertThat(result).isEqualTo(PAYLOAD);

    }

    @Test
    public void fetchExistingTransactionUsingRecipientKey() {

        final Response response = client.target(NODE2_URI)
            .path(RECEIVE_PATH)
            .request()
            .header(C11N_KEY, this.hash)
            .header(C11N_TO, PTY2_KEY)
            .buildGet()
            .invoke();

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final byte[] result = response.readEntity(byte[].class);

        assertThat(result).isEqualTo(PAYLOAD);

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
