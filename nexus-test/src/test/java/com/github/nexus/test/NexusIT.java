package com.github.nexus.test;

import org.junit.Ignore;
import org.junit.Test;

import javax.json.Json;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Base64;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriBuilder;

import static org.assertj.core.api.Assertions.assertThat;

public class NexusIT  {

    public static final URI SERVER_URI = UriBuilder.fromUri("http://localhost/").port(8080).build();
    
    private Client client = ClientBuilder.newClient();

    @Test
    public void sendSingleTransactionToSingleParty() {

        String sendRequest = Json.createObjectBuilder()
            .add("from", "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=")
            .add("to", Json.createArrayBuilder().add("yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0="))
            .add("payload", "Zm9v").build().toString();

        javax.ws.rs.core.Response response = client.target(SERVER_URI)
            .path("/transaction/send")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));


        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

    }

    /**
     * Quorum sends transaction with public key not in PartyInfo store.
     */
    @Ignore
    @Test
    public void sendSingleTransactionToMultipleParties() {
        String sendRequest = Json.createObjectBuilder()
            .add("from", "bXlwdWJsaWNrZXk=")
            .add("to", Json.createArrayBuilder()
                .add("cmVjaXBpZW50MQ==")
                .add(Base64.getEncoder().encodeToString("HELLOW".getBytes()))
            )
            .add("payload", "Zm9v").build().toString();

        javax.ws.rs.core.Response response = client.target(SERVER_URI)
            .path("/transaction/send")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));


        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);
    }

    /**
     * Quorum sends transaction with public key not in PartyInfo store.
     */
    @Ignore
    @Test
    public void sendUnknownPublicKey() {
        String sendRequest = Json.createObjectBuilder()
            .add("from", "bXlwdWJsaWNrZXk=")
            .add("to", Json.createArrayBuilder()
                .add(Base64.getEncoder().encodeToString("BOGUS".getBytes()))
            )
            .add("payload", "Zm9v").build().toString();

        javax.ws.rs.core.Response response = client.target(SERVER_URI)
            .path("/transaction/send")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));


        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Ignore
    @Test
    public void partyInfo() {

        InputStream data = new ByteArrayInputStream("SOMEDATA".getBytes());

        javax.ws.rs.core.Response response = client.target(SERVER_URI)
            .path("/partyinfo")
            .request()
            .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void upcheck() {
        javax.ws.rs.core.Response response = client.target(SERVER_URI)
            .path("/upcheck")
            .request()
            .get();

        assertThat(response).isNotNull();
        assertThat(response.readEntity(String.class))
            .isEqualTo("I'm up!");
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void requestVersion() {

        javax.ws.rs.core.Response response = client.target(SERVER_URI)
            .path("/version")
            .request()
            .get();

        assertThat(response).isNotNull();
        assertThat(response.readEntity(String.class))
            .isEqualTo("No version defined yet!");
        assertThat(response.getStatus()).isEqualTo(200);

    }

}
