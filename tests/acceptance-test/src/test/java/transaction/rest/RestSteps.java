package transaction.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.api.ReceiveResponse;
import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.api.SendResponse;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import com.quorum.tessera.test.rest.RestUtils;
import io.cucumber.java8.En;
import jakarta.json.Json;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;
import suite.ExecutionContext;
import transaction.utils.Utils;

public class RestSteps implements En {

  private final RestUtils restUtils = new RestUtils();

  private PartyHelper partyHelper = PartyHelper.create();

  public RestSteps() {

    final Collection<Party> senderHolder = new ArrayList<>();

    final Collection<String> responseCodes = new ArrayList<>();

    final Set<Party> recipients = new HashSet<>();

    final Set<String> storedHashes = new TreeSet<>();

    final byte[] txnData = Utils.generateTransactionData();

    Given(
        "^Sender party (.+)$",
        (String pty) -> {
          senderHolder.add(partyHelper.findByAlias(pty));
        });

    And(
        "^Recipient part(?:y|ies) (.+)$",
        (String alias) -> {
          parseAliases(alias).stream().map(partyHelper::findByAlias).forEach(recipients::add);

          assertThat(recipients).isNotEmpty();
        });

    And(
        "^all parties are running$",
        () -> {
          Client client = partyHelper.getParties().findAny().get().getRestClient();

          partyHelper
              .getParties()
              .forEach(
                  p -> {
                    Response response =
                        client.target(p.getP2PUri()).path("upcheck").request().get();

                    assertThat(response.getStatus())
                        .describedAs("P2P upcheck failed or party " + p.getAlias())
                        .isEqualTo(200);
                  });
        });

    When(
        "sender party receives transaction with an unknown party from Quorum peer",
        () -> {
          Party sender = senderHolder.stream().findAny().get();

          final SendRequest sendRequest = new SendRequest();
          sendRequest.setFrom(sender.getPublicKey());
          ExecutionContext executionContext = ExecutionContext.currentContext();
          String unknown =
              Utils.generateValidButUnknownPublicKey(executionContext.getEncryptorType())
                  .encodeToBase64();
          sendRequest.setTo(unknown);
          sendRequest.setPayload(txnData);

          final Response response =
              sender
                  .getRestClientWebTarget()
                  .path("send")
                  .request()
                  .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

          assertThat(response).isNotNull();
          assertThat(response.getStatus()).isEqualTo(404);
        });

    When(
        "^sender party receives transaction with no payload from Quorum peer$",
        () -> {
          Party sender = senderHolder.stream().findAny().get();

          List<String> recipientKeys =
              recipients.stream().map(Party::getPublicKey).collect(Collectors.toList());

          final String sendRequest =
              Json.createObjectBuilder()
                  .add("from", sender.getPublicKey())
                  .add("to", Json.createArrayBuilder(recipientKeys))
                  .build()
                  .toString();

          Response response =
              sender
                  .getRestClientWebTarget()
                  .path("send")
                  .request()
                  .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

          assertThat(response.getStatus()).isEqualTo(400);
        });

    When(
        "^sender party receives transaction with no sender key defined from Quorum peer$",
        () -> {
          Party sender = senderHolder.stream().findAny().get();
          SendRequest sendRequest = new SendRequest();

          sendRequest.setPayload(txnData);

          String[] recipientArray =
              recipients.stream()
                  .map(Party::getPublicKey)
                  .collect(Collectors.toList())
                  .toArray(new String[recipients.size()]);

          sendRequest.setTo(recipientArray);

          Response response =
              sender
                  .getRestClientWebTarget()
                  .path("send")
                  .request()
                  .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

          assertThat(response.getStatus()).isEqualTo(201);

          SendResponse sendResponse = response.readEntity(SendResponse.class);

          storedHashes.add(sendResponse.getKey());
        });

    When(
        "^sender party receives transaction from Quorum peer$",
        () -> {
          Party sender = senderHolder.stream().findAny().get();

          Response response = restUtils.send(sender, txnData, recipients);
          assertThat(response.getStatus()).isEqualTo(201);

          SendResponse sendResponse = response.readEntity(SendResponse.class);

          storedHashes.add(sendResponse.getKey());
        });

    Then(
        "^sender party stores the transaction$",
        () -> {
          Party sender = senderHolder.iterator().next();
          try (PreparedStatement statement =
              sender
                  .getDatabaseConnection()
                  .prepareStatement("SELECT COUNT(*) FROM ENCRYPTED_TRANSACTION WHERE HASH = ?")) {
            statement.setBytes(1, Base64.getDecoder().decode(storedHashes.iterator().next()));

            try (ResultSet results = statement.executeQuery()) {
              assertThat(results.next()).isTrue();
              assertThat(results.getLong(1)).isEqualTo(1);
            }
          }
        });

    Then(
        "^the response code is UNAUTHORIZED",
        () -> {
          assertThat(responseCodes).containsExactly("401");
        });

    Then(
        "^forwards the transaction to recipient part(?:y|ies)$",
        () -> {
          recipients.forEach(
              rec -> {
                final String storedHash = storedHashes.iterator().next();

                Response response = restUtils.findTransaction(storedHash, rec).findAny().get();

                assertThat(response.getStatus())
                    .describedAs(Objects.toString(response))
                    .isEqualTo(200);

                ReceiveResponse receiveResponse = response.readEntity(ReceiveResponse.class);

                assertThat(receiveResponse.getPayload()).isEqualTo(txnData);
              });

          partyHelper
              .getParties()
              .filter(p -> !senderHolder.contains(p))
              .filter(p -> !recipients.contains(p))
              .forEach(
                  p -> {
                    String storedHash = storedHashes.iterator().next();
                    Response response = restUtils.findTransaction(storedHash, p).findAny().get();
                    assertThat(response.getStatus()).isEqualTo(404);
                  });
        });

    Then(
        "^.*does not forward transaction to any recipients?$",
        () -> {
          partyHelper
              .getParties()
              .filter(p -> !senderHolder.contains(p))
              .forEach(
                  p -> {
                    String storedHash = storedHashes.stream().findAny().get();
                    Response response = restUtils.findTransaction(storedHash, p).findAny().get();
                    assertThat(response.getStatus()).isEqualTo(404);
                  });
        });

    Then(
        "an invalid request error is raised",
        () -> {
          // TODO: asserted in preceding step
        });
  }

  static List<String> parseAliases(String alias) {
    return Arrays.asList(alias.split(",| and "));
  }
}
