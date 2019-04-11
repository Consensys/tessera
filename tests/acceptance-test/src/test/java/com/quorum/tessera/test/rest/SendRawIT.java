package com.quorum.tessera.test.rest;

import com.quorum.tessera.api.model.ReceiveResponse;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import com.quorum.tessera.test.Party;
import static com.quorum.tessera.test.rest.RawHeaderName.*;

import static org.assertj.core.api.Assertions.assertThat;
import com.quorum.tessera.test.PartyHelper;

public class SendRawIT {

    private static final String SEND_PATH = "/sendraw";

    private RestUtils restUtils = new RestUtils();

    private final Client client = ClientBuilder.newClient();

    private static final byte[] TXN_DATA = "Zm9v".getBytes();

    private PartyHelper partyHelper = PartyHelper.create();

    private Party sender = partyHelper.findByAlias("A");

    private Party recipient = partyHelper.findByAlias("D");

    /**
     * Quorum sends transaction with singe public recipient key
     */
    @Test
    public void sendToSingleRecipient() {

        byte[] transactionData = restUtils.createTransactionData();

        final Response response = restUtils.sendRaw(sender, transactionData, recipient);

        //validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        String persistedKey = response.readEntity(String.class);
        assertThat(persistedKey).isNotNull();

        URI location = response.getLocation();

        final Response checkPersistedTxnResponse = client.target(location)
                .request()
                .get();

        assertThat(checkPersistedTxnResponse.getStatus())
                .isEqualTo(200);

        ReceiveResponse receiveResponse
                = checkPersistedTxnResponse.readEntity(ReceiveResponse.class);

        assertThat(receiveResponse.getPayload()).isEqualTo(transactionData);

        restUtils.findTransaction(persistedKey, sender, recipient).forEach(r -> {
            assertThat(r.getStatus()).isEqualTo(200);
        });

        restUtils.findTransaction(persistedKey, partyHelper.findByAlias("C"), partyHelper.findByAlias("B")).forEach(r -> {
            assertThat(r.getStatus()).isEqualTo(404);
        });

    }

    /**
     * Quorum sends transaction with multiple public recipient keys
     */
    @Test
    public void sendSingleTransactionToMultipleParties() {

        Party sender = partyHelper.findByAlias("A");
        Party firstRecipient = partyHelper.findByAlias("B");
        Party secondRecipient = partyHelper.findByAlias("D");

        byte[] transactionData = restUtils.createTransactionData();

        final Response response = restUtils.sendRaw(sender, transactionData, firstRecipient, secondRecipient);

        //validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        String persistedKey = response.readEntity(String.class);
        assertThat(persistedKey).isNotNull();

        URI location = response.getLocation();

        final Response checkPersistedTxnResponse = client.target(location)
                .request()
                .get();

        assertThat(checkPersistedTxnResponse.getStatus()).isEqualTo(200);

        ReceiveResponse receiveResponse = checkPersistedTxnResponse.readEntity(ReceiveResponse.class);

        assertThat(receiveResponse.getPayload()).isEqualTo(transactionData);

        restUtils.findTransaction(persistedKey, sender, firstRecipient, secondRecipient).forEach(r -> {
            assertThat(r.getStatus()).isEqualTo(200);
        });

        restUtils.findTransaction(persistedKey, partyHelper.findByAlias("C")).forEach(r -> {
            assertThat(r.getStatus()).isEqualTo(404);
        });

    }

    @Test
    public void sendTransactionWithoutASender() {

        URI uriToSendToWithoutPublicKey = partyHelper.findByAlias("A").getQ2TUri();
        Party recipient = partyHelper.findByAlias("D");

        byte[] transactionData = restUtils.createTransactionData();

        final Response response = client.target(uriToSendToWithoutPublicKey)
                .path(SEND_PATH)
                .request()
                .header(RECIPIENTS, recipient.getPublicKey())
                .post(Entity.entity(transactionData, MediaType.APPLICATION_OCTET_STREAM));

        //validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        String persistedKey = response.readEntity(String.class);
        assertThat(persistedKey).isNotNull();

        URI location = response.getLocation();

        final Response checkPersistedTxnResponse = client.target(location)
                .request()
                .get();

        assertThat(checkPersistedTxnResponse.getStatus()).isEqualTo(200);

        ReceiveResponse receiveResponse = checkPersistedTxnResponse.readEntity(ReceiveResponse.class);

        assertThat(receiveResponse.getPayload()).isEqualTo(transactionData);

        restUtils.findTransaction(persistedKey, partyHelper.findByAlias("A"), recipient).forEach(r -> {
            assertThat(r.getStatus()).isEqualTo(200);
        });

        restUtils.findTransaction(persistedKey, partyHelper.findByAlias("C"), partyHelper.findByAlias("B")).forEach(r -> {
            assertThat(r.getStatus()).isEqualTo(404);
        });
    }

    @Test
    public void sendTransactionWithEmptyRecipients() {

        Party sender = partyHelper.findByAlias("A");

        byte[] txnData = restUtils.createTransactionData();

        final Response response = restUtils.sendRaw(sender, txnData);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        String persistedKey = response.readEntity(String.class);
        assertThat(persistedKey).isNotNull();

        URI location = response.getLocation();

        final Response checkPersistedTxnResponse = client.target(location)
                .request()
                .get();

        assertThat(checkPersistedTxnResponse.getStatus()).isEqualTo(200);

        ReceiveResponse receiveResponse = checkPersistedTxnResponse.readEntity(ReceiveResponse.class);

        assertThat(receiveResponse.getPayload()).isEqualTo(txnData);
    }

    /**
     * In the case where no recipients are defined its is assumed that
     */
    @Test
    public void sendTransactionWithMissingRecipients() {

        Party sender = partyHelper.findByAlias("A");

        byte[] txnData = restUtils.createTransactionData();

        final Response response = restUtils.sendRaw(sender, txnData);

        //validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        String persistedKey = response.readEntity(String.class);
        assertThat(persistedKey).isNotNull();

        URI location = response.getLocation();

        final Response checkPersistedTxnResponse = client.target(location)
                .request()
                .get();

        assertThat(checkPersistedTxnResponse.getStatus()).isEqualTo(200);

        ReceiveResponse receiveResponse = checkPersistedTxnResponse.readEntity(ReceiveResponse.class);

        assertThat(receiveResponse.getPayload()).isEqualTo(txnData);
    }

    @Test
    public void missingPayloadFails() {

        final Response response = client.target(sender.getQ2TUri())
                .path(SEND_PATH)
                .request()
                .header(SENDER, sender.getPublicKey())
                .header(RECIPIENTS, recipient.getPublicKey())
                .post(Entity.entity(null, MediaType.APPLICATION_OCTET_STREAM));

        //validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(400);
    }

    /**
     * Quorum sends transaction with unknown public key
     */
    @Test
    public void sendUnknownPublicKey() {

        final Response response = client.target(sender.getQ2TUri())
                .path(SEND_PATH)
                .request()
                .header(SENDER, sender.getPublicKey())
                .header(RECIPIENTS, "8SjRHlUBe4hAmTk3KDeJ96RhN+s10xRrHDrxEi1O5W0=")
                .post(Entity.entity(TXN_DATA, MediaType.APPLICATION_OCTET_STREAM));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(400);
    }

}
