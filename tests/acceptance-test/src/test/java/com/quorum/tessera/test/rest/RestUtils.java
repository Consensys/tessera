package com.quorum.tessera.test.rest;

import static com.quorum.tessera.test.rest.RawHeaderName.RECIPIENTS;
import static com.quorum.tessera.test.rest.RawHeaderName.SENDER;
import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.api.SendResponse;
import com.quorum.tessera.test.Party;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import transaction.utils.Utils;

public class RestUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(RestUtils.class);

  public Response sendRaw(Party sender, byte[] transactionData, Set<Party> recipients) {
    return sendRaw(sender, transactionData, recipients.toArray(new Party[0]));
  }

  public Response sendRaw(Party sender, byte[] transactionData, Party... recipients) {

    Objects.requireNonNull(sender);

    String recipientString =
        Stream.of(recipients).map(Party::getPublicKey).collect(Collectors.joining(","));

    LOGGER.debug("Sending txn  to {}", recipientString);

    Invocation.Builder invocationBuilder =
        sender
            .getRestClientWebTarget()
            .path("sendraw")
            .request()
            .header(SENDER, sender.getPublicKey());

    Optional.of(recipientString)
        .filter(s -> !Objects.equals("", s))
        .ifPresent(s -> invocationBuilder.header(RECIPIENTS, s));

    return invocationBuilder.post(
        Entity.entity(transactionData, MediaType.APPLICATION_OCTET_STREAM));
  }

  public Stream<Response> findTransaction(String transactionId, Party... party) {

    String encodedId = urlEncode(transactionId);

    return Stream.of(party)
        .map(Party::getRestClientWebTarget)
        .map(target -> target.path("transaction"))
        .map(target -> target.path(encodedId))
        .map(target -> target.request().get());
  }

  static String urlEncode(String data) {
    try {
      return URLEncoder.encode(data, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException ex) {
      throw new RuntimeException(ex);
    }
  }

  public byte[] createTransactionData() {
    return generateTransactionData();
  }

  public static byte[] generateTransactionData() {
    return Utils.generateTransactionData();
  }

  public SendResponse sendRequestAssertSuccess(
      Party sender, byte[] transactionData, Party... recipients) {

    String[] recipientArray =
        Stream.of(recipients)
            .map(Party::getPublicKey)
            .collect(Collectors.toList())
            .toArray(new String[recipients.length]);

    final SendRequest sendRequest = new SendRequest();
    sendRequest.setFrom(sender.getPublicKey());
    sendRequest.setTo(recipientArray);
    sendRequest.setPayload(transactionData);

    final Response response =
        sender
            .getRestClientWebTarget()
            .path("send")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(201);
    return response.readEntity(SendResponse.class);
  }

  public Stream<Response> findTransaction(String transactionKey, Set<Party> recipients) {
    return findTransaction(transactionKey, recipients.toArray(new Party[0]));
  }

  public Response send(Party sender, byte[] transactionData, Set<Party> recipients) {
    return send(sender, transactionData, recipients.toArray(new Party[0]));
  }

  public Response send(Party sender, byte[] transactionData, Party... recipients) {
    String[] recipientArray =
        Stream.of(recipients)
            .map(Party::getPublicKey)
            .collect(Collectors.toList())
            .toArray(new String[recipients.length]);

    final SendRequest sendRequest = new SendRequest();

    sendRequest.setFrom(sender.getPublicKey());
    sendRequest.setTo(recipientArray);
    sendRequest.setPayload(transactionData);

    return sender
        .getRestClientWebTarget()
        .path("send")
        .request()
        .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));
  }

  private static final String C11N_TO = "c11n-to";

  private static final String C11N_KEY = "c11n-key";

  public Response receiveRaw(String transactionKey, Party party, Party... recipients) {
    return party
        .getRestClientWebTarget()
        .path("receiveraw")
        .request()
        .header(C11N_KEY, transactionKey)
        .header(
            C11N_TO,
            Stream.concat(Stream.of(recipients), Stream.of(party))
                .map(Party::getPublicKey)
                .collect(Collectors.joining(",")))
        .get();
  }
}
