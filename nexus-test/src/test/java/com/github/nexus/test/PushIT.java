package com.github.nexus.test;

import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.util.Base64;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static org.assertj.core.api.Assertions.assertThat;

public class PushIT {

    private static final URI SERVER_URI = UriBuilder.fromUri("http://127.0.0.1").port(8080).build();

    private static final String MSG_BASE64 = "AAAAAAAAAAIAAAAAAAAAwwAAAAAAAAAgyGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0AAAAAAAAAE8CbULKPZuEIm6N9a5Mlj46re6MAAAAAAAAAGEPsYS6j3eRGgZWbJAULVbXRi702/K8p0wAAAAAAAAABAAAAAAAAADAdTLvSlnbHxVmMe/0QlQ0HoFdmmqiiOCPQ4Qj238gQdTYl3lANeIuItWtcoTGSQkkAAAAAAAAAGHH4jjQvW2cluXrtd1P/CQyGpobmbsh0twAAAAAAAAAIAAAAAAAAAAA=";

    private static final String PUSH_PATH = "/push";

    private static final String ENCODED_HASH = "pVbEuhnKtLhIigze6oxgWSROMeTk8uCX%2B0KkOyfTKn9PwW9UJjIYZa%2B4t8I6dn8apnl2C9Bc9gFfIYKxl2j4Sw%3D%3D";

    private final Client client = ClientBuilder.newClient();

    private byte[] message;

    @Before
    public void init() {
        this.message = Base64.getDecoder().decode(MSG_BASE64);

        //delete the tx if it exists, or do nothing if it doesn't
        client.target(SERVER_URI)
            .path("/transaction/" + ENCODED_HASH)
            .request()
            .buildDelete()
            .invoke();
    }

    @Test
    public void storePayloadFromAnotherNode() {

        final Response pushReponse = client.target(SERVER_URI)
            .path(PUSH_PATH)
            .request()
            .post(Entity.entity(message, APPLICATION_OCTET_STREAM));


        assertThat(pushReponse).isNotNull();
        assertThat(pushReponse.getStatus()).isEqualTo(201);

        //retrieve that tx

        final Response retrieveResponse = client.target(SERVER_URI)
            .path("/transaction/" + ENCODED_HASH)
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
    public void storeExistingPayloadThrowsError() {
        final Response pushReponse = client.target(SERVER_URI)
            .path(PUSH_PATH)
            .request()
            .post(Entity.entity(message, APPLICATION_OCTET_STREAM));

        assertThat(pushReponse).isNotNull();
        assertThat(pushReponse.getStatus()).isEqualTo(201);

        //send it again

        final Response pushReponseDup = client.target(SERVER_URI)
            .path(PUSH_PATH)
            .request()
            .post(Entity.entity(message, APPLICATION_OCTET_STREAM));

        assertThat(pushReponseDup).isNotNull();
        //TODO: should this be a 400?
        assertThat(pushReponseDup.getStatus()).isEqualTo(500);
    }

    @Test
    public void storeCorruptedPayloadFails() {

        final byte[] badPayload = "this is a bad payload that does not conform to the expected byte array".getBytes();

        final Response pushReponse = client.target(SERVER_URI)
            .path(PUSH_PATH)
            .request()
            .post(Entity.entity(badPayload, APPLICATION_OCTET_STREAM));

        assertThat(pushReponse).isNotNull();
        //TODO: should be 400?
        assertThat(pushReponse.getStatus()).isEqualTo(500);
    }

}
