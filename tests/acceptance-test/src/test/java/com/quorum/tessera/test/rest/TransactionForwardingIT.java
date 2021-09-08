package com.quorum.tessera.test.rest;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.api.ReceiveResponse;
import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.api.SendResponse;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import suite.NodeAlias;

/**
 * Tests that recipients specified in the forwarding list receive a transaction
 *
 * <p>Node 3 should send all transactions to node 1
 */
public class TransactionForwardingIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionForwardingIT.class);

  private Party sender;

  private Party reciepient;

  private Party otherRecipient;

  private final PartyHelper parytyHelper = PartyHelper.create();

  private byte[] transactionData;

  @Before
  public void beforeTest() {
    transactionData = UUID.randomUUID().toString().getBytes();
    sender = parytyHelper.findByAlias(NodeAlias.A);

    reciepient = parytyHelper.findByAlias(NodeAlias.B);

    otherRecipient = parytyHelper.findByAlias(NodeAlias.C);
  }

  @Test
  public void sendTransactionToNode3AddsNode1AsRecipient() throws UnsupportedEncodingException {

    final String hash = this.sendNewTransaction(otherRecipient);

    // check the transaction is in node 1
    final Response response =
        sender
            .getRestClient()
            .target(sender.getQ2TUri())
            .path("transaction")
            .path(URLEncoder.encode(hash, UTF_8.toString()))
            .property("to", URLEncoder.encode(sender.getPublicKey(), UTF_8.toString()))
            .request()
            .get();

    // validate result
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);

    final ReceiveResponse result = response.readEntity(ReceiveResponse.class);

    assertThat(result.getPayload()).isEqualTo(transactionData);
  }

  @Test
  public void sendTransactionToNode2DoesNotAddNode1AsRecipient()
      throws UnsupportedEncodingException {

    final String hash = this.sendNewTransaction(reciepient);

    // check the transaction is not in node 1
    final Response response =
        sender
            .getRestClient()
            .target(sender.getQ2TUri())
            .path("transaction")
            .path(URLEncoder.encode(hash, UTF_8.toString()))
            .property("to", URLEncoder.encode(sender.getPublicKey(), UTF_8.toString()))
            .request()
            .get();

    // validate result
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(404);
  }

  @Test
  public void sendTransactionToNode3DoesNotAddNode2AsRecipient()
      throws UnsupportedEncodingException {

    final String hash = this.sendNewTransaction(otherRecipient);

    // check the transaction is in node 1
    final Response response =
        sender
            .getRestClient()
            .target(reciepient.getQ2TUri())
            .path("transaction")
            .path(URLEncoder.encode(hash, UTF_8.toString()))
            .property("to", URLEncoder.encode(sender.getPublicKey(), UTF_8.toString()))
            .request()
            .get();

    // validate result
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(404);
  }

  private String sendNewTransaction(final Party party) {

    final URI node = party.getQ2TUri();
    final String from = party.getPublicKey();

    SendRequest sendRequest = new SendRequest();
    sendRequest.setFrom(from);
    sendRequest.setPayload(transactionData);

    LOGGER.debug("Sending {} to {}", sendRequest, node);

    final Response response =
        party
            .getRestClient()
            .target(node)
            .path("/send")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(201);

    LOGGER.debug("Sent {} to {}", sendRequest, node);
    // check the call was success
    final SendResponse result = response.readEntity(SendResponse.class);
    LOGGER.debug("Response status : {}, body: {}", response.getStatus(), result);

    assertThat(result.getKey()).isNotBlank();

    return result.getKey();
  }
}
