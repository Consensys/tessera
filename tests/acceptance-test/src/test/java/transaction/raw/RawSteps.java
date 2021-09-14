package transaction.raw;

import static com.quorum.tessera.test.rest.RawHeaderName.RECIPIENTS;
import static com.quorum.tessera.test.rest.RawHeaderName.SENDER;
import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.api.ReceiveResponse;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import com.quorum.tessera.test.rest.RestUtils;
import io.cucumber.java8.En;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import suite.ExecutionContext;

public class RawSteps implements En {

  private RestUtils restUtils = new RestUtils();

  private PartyHelper partyHelper = PartyHelper.create();

  private Party getSender(Collection<String> senderHolder) {
    return partyHelper.findByAlias(senderHolder.stream().findAny().get());
  }

  private Set<Party> getRecipientParties(Set<String> recipientAliases) {
    return recipientAliases.stream().map(partyHelper::findByAlias).collect(Collectors.toSet());
  }

  public RawSteps() {

    final Collection<String> senderHolder = new ArrayList<>();

    final Set<String> recipients = new HashSet<>();

    final byte[] transactionData = restUtils.createTransactionData();

    final Set<String> storedHashes = new TreeSet<>();

    Given(
        "^Sender party (.+)$",
        (String pty) -> {
          senderHolder.add(pty);
        });

    And(
        "^Recipient part(?:y|ies) (.+)$",
        (String alias) -> {
          parseAliases(alias).stream().forEach(recipients::add);

          assertThat(recipients).isNotEmpty();
        });

    And(
        "^all parties are running$",
        () -> {
          final Client client = partyHelper.getParties().findAny().get().getRestClient();

          assertThat(
                  partyHelper
                      .getParties()
                      .map(Party::getP2PUri)
                      .map(client::target)
                      .map(t -> t.path("upcheck"))
                      .map(WebTarget::request)
                      .map(Invocation.Builder::get)
                      .allMatch(r -> r.getStatus() == 200))
              .describedAs("All serers are up ")
              .isTrue();
        });

    When(
        "^sender party receives transaction from Quorum peer$",
        () -> {
          Party sender = getSender(senderHolder);

          Response response =
              restUtils.sendRaw(
                  sender, transactionData, getRecipientParties(recipients).toArray(new Party[0]));

          assertThat(response.getStatus()).isEqualTo(200);

          String persistedKey = response.readEntity(String.class);
          assertThat(persistedKey).isNotNull();

          storedHashes.add(persistedKey);
        });

    When(
        "sender party receives transaction with no sender key defined from Quorum peer",
        () -> {
          Party sender = getSender(senderHolder);

          final Response response =
              sender
                  .getRestClientWebTarget()
                  .path("sendraw")
                  .request()
                  .header(
                      RECIPIENTS,
                      recipients.stream()
                          .map(partyHelper::findByAlias)
                          .map(Party::getPublicKey)
                          .collect(Collectors.joining(",")))
                  .post(Entity.entity(transactionData, MediaType.APPLICATION_OCTET_STREAM));

          assertThat(response).isNotNull();
          assertThat(response.getStatus()).isEqualTo(200);

          String persistedKey = response.readEntity(String.class);
          assertThat(persistedKey).isNotNull();
          storedHashes.add(persistedKey);

          URI location = response.getLocation();

          recipients.stream()
              .map(partyHelper::findByAlias)
              .map(Party::getRestClient)
              .forEach(
                  client -> {
                    final Response checkPersistedTxnResponse =
                        client.target(location).request().get();

                    assertThat(checkPersistedTxnResponse.getStatus()).isEqualTo(200);

                    ReceiveResponse receiveResponse =
                        checkPersistedTxnResponse.readEntity(ReceiveResponse.class);

                    assertThat(receiveResponse.getPayload()).isEqualTo(transactionData);

                    final Set<Party> recipientParties = getRecipientParties(recipients);

                    List<Response> responses =
                        restUtils
                            .findTransaction(persistedKey, recipientParties)
                            .collect(Collectors.toList());

                    responses.forEach(
                        r -> {
                          assertThat(r.getStatus())
                              .describedAs("find transaction for " + recipients + ". " + r)
                              .isEqualTo(200);
                        });
                  });

          restUtils
              .findTransaction(
                  persistedKey, partyHelper.findByAlias("C"), partyHelper.findByAlias("B"))
              .forEach(
                  r -> {
                    assertThat(r.getStatus()).isEqualTo(404);
                  });
        });

    When(
        "sender party receives transaction with no payload from Quorum peer",
        () -> {
          Party sender = getSender(senderHolder);

          Response response = restUtils.sendRaw(sender, null, getRecipientParties(recipients));

          assertThat(response).isNotNull();
          assertThat(response.getStatus()).isEqualTo(400);
        });

    When(
        "sender party receives transaction with an unknown party from Quorum peer",
        () -> {
          Party sender = getSender(senderHolder);

          ExecutionContext executionContext = ExecutionContext.currentContext();
          String unknown =
              transaction.utils.Utils.generateValidButUnknownPublicKey(
                      executionContext.getEncryptorType())
                  .encodeToBase64();

          final Response response =
              sender
                  .getRestClientWebTarget()
                  .path("sendraw")
                  .request()
                  .header(SENDER, sender.getPublicKey())
                  .header(RECIPIENTS, unknown)
                  .post(Entity.entity(transactionData, MediaType.APPLICATION_OCTET_STREAM));

          assertThat(response).isNotNull();
          assertThat(response.getStatus()).isEqualTo(404);
        });

    Then(
        "an invalid request error is raised",
        () -> {
          // FIXME: validated in sending function
        });

    Then(
        "^sender party stores the transaction$",
        () -> {
          Party sender = getSender(senderHolder);
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
        "^forwards the transaction to recipient part(?:y|ies)$",
        () -> {
          recipients.stream()
              .map(partyHelper::findByAlias)
              .forEach(
                  rec -> {
                    String storedHash = storedHashes.stream().findAny().get();
                    Response response = restUtils.receiveRaw(storedHash, rec);

                    assertThat(response.getStatus()).isEqualTo(200);
                    final byte[] result = response.readEntity(byte[].class);
                    assertThat(result).isEqualTo(transactionData);
                  });
        });

    Then(
        "^.*does not forward transaction to any recipients?$",
        () -> {
          partyHelper
              .getParties()
              .filter(p -> !senderHolder.contains(p.getAlias()))
              .forEach(
                  p -> {
                    String storedHash = storedHashes.stream().findAny().get();
                    Response response = restUtils.receiveRaw(storedHash, p);
                    assertThat(response.getStatus()).isEqualTo(404);
                  });
        });
  }

  static List<String> parseAliases(String alias) {
    return Arrays.asList(alias.split(",| and "));
  }
}
