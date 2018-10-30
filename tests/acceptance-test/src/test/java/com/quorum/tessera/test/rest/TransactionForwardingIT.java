package com.quorum.tessera.test.rest;

import com.quorum.tessera.api.model.ReceiveResponse;
import com.quorum.tessera.api.model.SendRequest;
import com.quorum.tessera.api.model.SendResponse;
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Tests that recipients specified in the forwarding list receive a transaction
 * <p>
 * Node 3 should send all transactions to node 1
 */
public class TransactionForwardingIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionForwardingIT.class);

    private static final URI NODE_ONE = NODE1_Q2T_URI;

    private static final URI NODE_TWO = NODE2_Q2T_URI;

    private static final URI NODE_THREE = NODE3_Q2T_URI;

    private final Client client = ClientBuilder.newClient();

    @Test
    public void sendTransactionToNode3AddsNode1AsRecipient() throws UnsupportedEncodingException {

        final String hash = this.sendNewTransaction(NODE_THREE, PTY3_KEY);

        //check the transaction is in node 1
        final Response response = this.client.target(NODE_ONE)
                .path("transaction")
                .path(URLEncoder.encode(hash, UTF_8.toString()))
                .property("to", URLEncoder.encode(PTY1_KEY, UTF_8.toString()))
                .request()
                .get();

        //validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final ReceiveResponse result = response.readEntity(ReceiveResponse.class);

        assertThat(result.getPayload())
                .isEqualTo(TXN_DATA);

    }

    @Test
    public void sendTransactionToNode2DoesNotAddNode1AsRecipient() throws UnsupportedEncodingException {

        final String hash = this.sendNewTransaction(NODE_TWO, PTY2_KEY);

        //check the transaction is not in node 1
        final Response response = this.client.target(NODE_ONE)
                .path("transaction")
                .path(URLEncoder.encode(hash, UTF_8.toString()))
                .property("to", URLEncoder.encode(PTY1_KEY, UTF_8.toString()))
                .request()
                .get();

        //validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(404);

    }

    @Test
    public void sendTransactionToNode3DoesNotAddNode2AsRecipient() throws UnsupportedEncodingException {

        final String hash = this.sendNewTransaction(NODE_THREE, PTY3_KEY);

        //check the transaction is in node 1
        final Response response = this.client.target(NODE_TWO)
                .path("transaction")
                .path(URLEncoder.encode(hash, UTF_8.toString()))
                .property("to", URLEncoder.encode(PTY1_KEY, UTF_8.toString()))
                .request()
                .get();

        //validate result
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(404);

    }

    /**
     * Sends a new transaction to the given node
     *
     * @param node the target node for the new transaction
     * @return the hash of the transaction
     */
    private String sendNewTransaction(final URI node, final String from) {

        SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(from);
        sendRequest.setPayload(TXN_DATA);

        LOGGER.debug("Sending {} to {}", sendRequest, node);

        final Response response = this.client.target(node)
                .path("/send")
                .request()
                .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(201);

        LOGGER.debug("Sent {} to {}", sendRequest, node);
        //check the call was success
        final SendResponse result = response.readEntity(SendResponse.class);
        LOGGER.debug("Response status : {}, body: {}", response.getStatus(), result);

        assertThat(result.getKey()).isNotBlank();

        return result.getKey();
    }

}
