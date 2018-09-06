package com.quorum.tessera.test.rest;

import com.quorum.tessera.api.model.ResendRequest;
import com.quorum.tessera.api.model.ResendRequestType;
import com.quorum.tessera.transaction.PayloadEncoder;
import com.quorum.tessera.transaction.PayloadEncoderImpl;
import com.quorum.tessera.transaction.model.EncodedPayloadWithRecipients;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class ResendIndividualIT {

    private static final URI SERVER_URI = UriBuilder.fromUri("http://127.0.0.1").port(8080).build();

    private static final Client client = ClientBuilder.newClient();

    private static final String RESEND_PATH = "/resend";

    private static final String SENDER_KEY = "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=";

    private static final String RECIPIENT_KEY = "yGcjkFyZklTTXrn8+WIkYwicA2EGBn9wZFkctAad4X0=";

    private static final PayloadEncoder ENCODER = new PayloadEncoderImpl();

    private String hash;

    @Before
    public void init() {
        final Response response = client.target(SERVER_URI)
            .path("/sendraw")
            .request()
            .header("c11n-from", SENDER_KEY)
            .header("c11n-to", RECIPIENT_KEY)
            .post(Entity.entity("Zm9v".getBytes(), MediaType.APPLICATION_OCTET_STREAM));

        //validate result

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
        this.hash = response.readEntity(String.class);
    }

    @Test
    public void resendTransactionsForGivenKey() {

        final ResendRequest request = new ResendRequest();
        request.setType(ResendRequestType.INDIVIDUAL);
        request.setKey(this.hash);
        request.setPublicKey(RECIPIENT_KEY);

        final Response response = client.target(SERVER_URI)
            .path(RESEND_PATH)
            .request()
            .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final byte[] returnValue = response.readEntity(byte[].class);
        final EncodedPayloadWithRecipients payloadWithRecipients = ENCODER.decodePayloadWithRecipients(returnValue);

        assertThat(payloadWithRecipients).isNotNull();
    }

    @Test
    public void resendTransactionForIncorrectKey() {
        final ResendRequest request = new ResendRequest();
        request.setType(ResendRequestType.INDIVIDUAL);
        request.setKey(this.hash);
        request.setPublicKey("giizjhZQM6peq52O7icVFxdTmTYinQSUsvyhXzgZqkE=");

        final Response response = client.target(SERVER_URI)
            .path(RESEND_PATH)
            .request()
            .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.readEntity(String.class))
            .contains("Recipient giizjhZQM6peq52O7icVFxdTmTYinQSUsvyhXzgZqkE= is not a recipient of transaction");

    }

    @Test
    public void resendTransactionThatDoesntExist() {

        final String unknownHash = "2xTEBlTtYXSBXZD4jDDp83cVJbnkzP6PbUoUJx076BO/FSR75NXwDDpLDu3AIiDV1TlK8nGK4mlhsg4Xzpd5og==";


        final ResendRequest request = new ResendRequest();
        request.setType(ResendRequestType.INDIVIDUAL);
        request.setKey(unknownHash);
        request.setPublicKey("giizjhZQM6peq52O7icVFxdTmTYinQSUsvyhXzgZqkE=");

        final Response response = client.target(SERVER_URI)
            .path(RESEND_PATH)
            .request()
            .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.readEntity(String.class)).contains("Message with hash " + unknownHash + " was not found");

    }



}
