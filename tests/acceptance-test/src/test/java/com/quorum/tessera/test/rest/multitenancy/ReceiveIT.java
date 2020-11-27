package com.quorum.tessera.test.rest.multitenancy;

import com.quorum.tessera.api.ReceiveResponse;
import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.api.SendResponse;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

import static com.quorum.tessera.version.MultiTenancyVersion.MIME_TYPE_JSON_2_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/** retrieve tx with hash retrieve tx with hash and sender retrieve hash that doesn't exist */
public class ReceiveIT {

    private static final String RECEIVE_PATH = "transaction";

    private static final Client client = ClientBuilder.newClient();

    private byte[] transactionData = UUID.randomUUID().toString().getBytes();

    private String encodedHash;

    private String encodedSender;

    private String encodedRecipient;

    private Party partyOne;

    private Party partyTwo;

    // Persist a single transaction that can be used later
    @Before
    public void init() throws UnsupportedEncodingException {
        final PartyHelper partyHelper = PartyHelper.create();
        partyOne = partyHelper.findByAlias("A");
        partyTwo = partyHelper.findByAlias("B");

        SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(partyOne.getPublicKey());
        sendRequest.setTo(partyTwo.getPublicKey());
        sendRequest.setPayload(transactionData);

        final Response response =
                client.target(partyOne.getQ2TUri())
                        .path("/send")
                        .request()
                        .post(Entity.entity(sendRequest, MIME_TYPE_JSON_2_1));

        assertThat(response.getStatus()).isEqualTo(201);

        final SendResponse result = response.readEntity(SendResponse.class);

        final String hash = result.getKey();

        this.encodedHash = URLEncoder.encode(hash, UTF_8.toString());
        this.encodedSender = URLEncoder.encode(partyOne.getPublicKey(), UTF_8.toString());
        this.encodedRecipient = URLEncoder.encode(partyTwo.getPublicKey(), UTF_8.toString());
    }

    //    @Test
    public void fetchExistingTransactionUsingOwnKey() {

        final Response response =
                client.target(partyOne.getQ2TUri())
                        .path(RECEIVE_PATH + "/" + this.encodedHash)
                        .queryParam("to", this.encodedSender)
                        .request()
                        .accept(MIME_TYPE_JSON_2_1)
                        .buildGet()
                        .invoke();

        // validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final ReceiveResponse result = response.readEntity(ReceiveResponse.class);

        assertThat(result.getPayload()).isEqualTo(transactionData);
        assertThat(result.getManagedParties()).containsExactly(partyOne.getPublicKey());
    }

    @Test
    public void fetchExistingTransactionUsingRecipientKey() {

        final Response response =
                client.target(partyTwo.getQ2TUri())
                        .path(RECEIVE_PATH)
                        .queryParam("to", this.encodedRecipient)
                        .path(encodedHash)
                        .request()
                        .accept(MIME_TYPE_JSON_2_1)
                        .get();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final ReceiveResponse result = response.readEntity(ReceiveResponse.class);

        assertThat(result.getPayload()).isEqualTo(transactionData);
        assertThat(result.getManagedParties()).containsExactly(partyTwo.getPublicKey());
    }

    @Test
    public void fetchExistingTransactionNotUsingKey() {

        final Response response =
                client.target(partyOne.getQ2TUri())
                        .path(RECEIVE_PATH)
                        .path(encodedHash)
                        .request()
                        .accept(MIME_TYPE_JSON_2_1)
                        .buildGet()
                        .invoke();

        // validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final ReceiveResponse result = response.readEntity(ReceiveResponse.class);

        assertThat(result.getPayload()).isEqualTo(transactionData);
        assertThat(result.getManagedParties()).containsExactly(partyOne.getPublicKey());
    }

    @Test
    public void fetchNonexistantTransactionFails() {

        final Response response =
                client.target(partyOne.getQ2TUri())
                        .path(RECEIVE_PATH)
                        .path("invalidhashvalue")
                        .request()
                        .accept(MIME_TYPE_JSON_2_1)
                        .get();

        // validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(404);

        final String result = response.readEntity(String.class);
        assertThat(result).isEqualTo("Message with hash invalidhashvalue was not found");
    }
}
