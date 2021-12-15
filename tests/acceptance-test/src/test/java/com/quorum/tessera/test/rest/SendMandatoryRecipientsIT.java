package com.quorum.tessera.test.rest;

import static com.quorum.tessera.version.MandatoryRecipientsVersion.MIME_TYPE_JSON_4;
import static com.quorum.tessera.version.MultiTenancyVersion.MIME_TYPE_JSON_2_1;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.api.*;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadCodec;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import db.UncheckedSQLException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import org.junit.Test;
import suite.NodeAlias;

public class SendMandatoryRecipientsIT {

  private final PartyHelper partyHelper = PartyHelper.create();

  private RestUtils utils = new RestUtils();

  final Party a = partyHelper.findByAlias(NodeAlias.A);
  final Party b = partyHelper.findByAlias(NodeAlias.B);
  final Party c = partyHelper.findByAlias(NodeAlias.C);

  final PayloadEncoder encoder = PayloadEncoder.create(EncodedPayloadCodec.current());

  @Test
  public void invalidRequests() {

    byte[] transactionData = utils.createTransactionData();

    final SendRequest sendRequest = new SendRequest();
    sendRequest.setPayload(transactionData);
    sendRequest.setTo(b.getPublicKey());
    sendRequest.setPrivacyFlag(2);
    sendRequest.setMandatoryRecipients(c.getPublicKey());

    final Response response =
        a.getRestClient()
            .target(a.getQ2TUri())
            .path("/send")
            .request()
            .post(Entity.entity(sendRequest, MIME_TYPE_JSON_4));

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Test
  public void sendTransactionWithMandatoryRecipients() throws UnsupportedEncodingException {

    byte[] transactionData = utils.createTransactionData();

    final SendRequest sendRequest = new SendRequest();
    sendRequest.setPayload(transactionData);
    sendRequest.setTo(b.getPublicKey(), c.getPublicKey());
    sendRequest.setPrivacyFlag(2);
    sendRequest.setMandatoryRecipients(c.getPublicKey());

    final Response response =
        a.getRestClient()
            .target(a.getQ2TUri())
            .path("/send")
            .request()
            .post(Entity.entity(sendRequest, MIME_TYPE_JSON_4));

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(201);

    final SendResponse result = response.readEntity(SendResponse.class);
    final String hash = result.getKey();

    assertThat(hash).isNotNull().isNotBlank();

    // Hash length should be 64 bytes
    final byte[] hashBytes = Base64.getDecoder().decode(hash);
    assertThat(hashBytes).hasSize(64);

    // validate data in Node A's DB
    Connection connection = a.getDatabaseConnection();

    byte[] receivedPayload;

    try (connection) {

      PreparedStatement preparedStatement =
          connection.prepareStatement(
              "SELECT ENCODED_PAYLOAD FROM ENCRYPTED_TRANSACTION et WHERE HASH = ?");
      preparedStatement.setBytes(1, hashBytes);
      try (preparedStatement) {
        ResultSet resultSet = preparedStatement.executeQuery();
        try (resultSet) {
          assertThat(resultSet.next()).isTrue();
          receivedPayload = resultSet.getBytes(1);
        }
      }
    } catch (SQLException ex) {
      throw new UncheckedSQLException(ex);
    }

    EncodedPayload payload = encoder.decode(receivedPayload);
    assertThat(payload.getMandatoryRecipients())
        .containsExactly(PublicKey.from(Base64.getDecoder().decode(c.getPublicKey())));

    // validate data in Node C's DB
    Connection connectionC = c.getDatabaseConnection();

    byte[] payloadOnC;

    try (connectionC) {

      PreparedStatement preparedStatement =
          connectionC.prepareStatement(
              "SELECT ENCODED_PAYLOAD FROM ENCRYPTED_TRANSACTION et WHERE HASH = ?");
      preparedStatement.setBytes(1, hashBytes);
      try (preparedStatement) {
        ResultSet resultSet = preparedStatement.executeQuery();
        try (resultSet) {
          assertThat(resultSet.next()).isTrue();
          payloadOnC = resultSet.getBytes(1);
        }
      }
    } catch (SQLException ex) {
      throw new UncheckedSQLException(ex);
    }

    EncodedPayload payloadC = encoder.decode(payloadOnC);
    assertThat(payloadC.getMandatoryRecipients())
        .containsExactly(PublicKey.from(Base64.getDecoder().decode(c.getPublicKey())));

    // Validate recipients data in Node C using /mandatory call

    final String encodedHash = URLEncoder.encode(hash, UTF_8.toString());

    final Response getMandatoryResponse =
        c.getRestClient()
            .target(c.getQ2TUri())
            .path("/transaction")
            .path(encodedHash)
            .path("/mandatory")
            .request()
            .buildGet()
            .invoke();

    assertThat(getMandatoryResponse).isNotNull();
    assertThat(getMandatoryResponse.getStatus()).isEqualTo(200);
    String mandatoryList = getMandatoryResponse.readEntity(String.class);
    assertThat(mandatoryList).isEqualTo(c.getPublicKey());
  }

  @Test
  public void sendSignedTransactionWithMandatoryRecipients() throws UnsupportedEncodingException {

    byte[] transactionData = utils.createTransactionData();

    final StoreRawRequest storeRawRequest = new StoreRawRequest();
    storeRawRequest.setPayload(transactionData);

    final Response storeRawResponse =
        a.getRestClient()
            .target(a.getThirdPartyUri())
            .path("/storeraw")
            .request()
            .post(Entity.entity(storeRawRequest, APPLICATION_JSON));

    assertThat(storeRawResponse).isNotNull();
    assertThat(storeRawResponse.getStatus()).isEqualTo(200);

    final StoreRawResponse result = storeRawResponse.readEntity(StoreRawResponse.class);
    final byte[] hash = result.getKey();

    // Hash length should be 64 bytes
    assertThat(hash).hasSize(64);

    final String encodedHash =
        URLEncoder.encode(Base64.getEncoder().encodeToString(hash), UTF_8.toString());

    final Response retrieve =
        a.getRestClient()
            .target(a.getQ2TUri())
            .path("/transaction")
            .path(encodedHash)
            .request()
            .accept(MIME_TYPE_JSON_2_1)
            .buildGet()
            .invoke();

    // Transaction has not yet been distributed at this point
    assertThat(retrieve.getStatus()).isEqualTo(404);

    final SendSignedRequest sendSignedRequest = new SendSignedRequest();
    sendSignedRequest.setTo(b.getPublicKey(), c.getPublicKey());
    sendSignedRequest.setHash(hash);
    sendSignedRequest.setPrivacyFlag(2);
    sendSignedRequest.setMandatoryRecipients(c.getPublicKey());

    final Response response =
        a.getRestClient()
            .target(a.getQ2TUri())
            .path("/sendsignedtx")
            .request()
            .post(Entity.entity(sendSignedRequest, MIME_TYPE_JSON_4));

    assertThat(response.getStatus()).isEqualTo(201);

    // validate data in Node A's DB
    Connection connection = a.getDatabaseConnection();

    byte[] payloadOnA;

    try (connection) {

      PreparedStatement preparedStatement =
          connection.prepareStatement(
              "SELECT ENCODED_PAYLOAD FROM ENCRYPTED_TRANSACTION et WHERE HASH = ?");
      preparedStatement.setBytes(1, hash);
      try (preparedStatement) {
        ResultSet resultSet = preparedStatement.executeQuery();
        try (resultSet) {
          assertThat(resultSet.next()).isTrue();
          payloadOnA = resultSet.getBytes(1);
        }
      }
    } catch (SQLException ex) {
      throw new UncheckedSQLException(ex);
    }

    EncodedPayload payload = encoder.decode(payloadOnA);
    assertThat(payload.getMandatoryRecipients())
        .containsExactly(PublicKey.from(Base64.getDecoder().decode(c.getPublicKey())));

    // validate data in Node C's DB
    Connection connectionC = c.getDatabaseConnection();

    byte[] payloadOnC;

    try (connectionC) {

      PreparedStatement preparedStatement =
          connectionC.prepareStatement(
              "SELECT ENCODED_PAYLOAD FROM ENCRYPTED_TRANSACTION et WHERE HASH = ?");
      preparedStatement.setBytes(1, hash);
      try (preparedStatement) {
        ResultSet resultSet = preparedStatement.executeQuery();
        try (resultSet) {
          assertThat(resultSet.next()).isTrue();
          payloadOnC = resultSet.getBytes(1);
        }
      }
    } catch (SQLException ex) {
      throw new UncheckedSQLException(ex);
    }

    EncodedPayload payloadC = encoder.decode(payloadOnC);
    assertThat(payloadC.getMandatoryRecipients())
        .containsExactly(PublicKey.from(Base64.getDecoder().decode(c.getPublicKey())));
  }
}
