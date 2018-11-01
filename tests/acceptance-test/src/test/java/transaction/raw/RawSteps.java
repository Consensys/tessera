package transaction.raw;

import com.quorum.tessera.api.model.ReceiveResponse;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import com.quorum.tessera.test.RestPartyHelper;
import static com.quorum.tessera.test.rest.RawHeaderName.RECIPIENTS;
import static com.quorum.tessera.test.rest.RawHeaderName.SENDER;
import com.quorum.tessera.test.rest.RestUtils;
import cucumber.api.java8.En;
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
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;

public class RawSteps implements En {

    private RestUtils restUtils = new RestUtils();

    private PartyHelper partyHelper = new RestPartyHelper();

    private final Client client = RestUtils.buildClient();

    public RawSteps() {

        Collection<Party> senderHolder = new ArrayList<>();

        Set<Party> recipients = new HashSet<>();

        byte[] transactionData = restUtils.createTransactionData();

        Set<String> storedHashes = new TreeSet<>();

        Given("^Sender party (.+)$", (String pty) -> {
            Party sender = partyHelper.findByAlias(pty);
            senderHolder.add(sender);
        });

        And("^Recipient part(?:y|ies) (.+)$", (String alias) -> {
            parseAliases(alias).stream()
                .map(partyHelper::findByAlias)
                .forEach(recipients::add);

            assertThat(recipients).isNotEmpty();
        });

        And("^all parties are running$", () -> {

            assertThat(partyHelper.getParties()
                .map(Party::getP2PUri)
                .map(client::target)
                .map(t -> t.path("upcheck"))
                .map(WebTarget::request)
                .map(Invocation.Builder::get)
                .allMatch(r -> r.getStatus() == 200))
                .isTrue();
        });

        When("^sender party receives transaction from Quorum peer$", () -> {

            Party sender = senderHolder.stream().findAny().get();

            Response response = restUtils.sendRaw(sender,
                transactionData,
                recipients.toArray(new Party[0]));

            assertThat(response.getStatus()).isEqualTo(200);

            String persistedKey = response.readEntity(String.class);
            assertThat(persistedKey).isNotNull();

            storedHashes.add(persistedKey);

        });

        When("sender party receives transaction with no sender key defined from Quorum peer", () -> {
            Party sender = senderHolder.stream().findAny().get();
            
            final Response response = client.target(sender.getQ2TUri())
                .path("sendraw")
                .request()
                .header(RECIPIENTS, recipients.stream()
                    .map(Party::getPublicKey)
                    .collect(Collectors.joining(","))
                )
                .post(Entity.entity(transactionData, MediaType.APPLICATION_OCTET_STREAM));

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(200);

            String persistedKey = response.readEntity(String.class);
            assertThat(persistedKey).isNotNull();
            storedHashes.add(persistedKey);

            URI location = response.getLocation();

            final Response checkPersistedTxnResponse = client.target(location)
                .request()
                .get();

            assertThat(checkPersistedTxnResponse.getStatus()).isEqualTo(200);

            ReceiveResponse receiveResponse = checkPersistedTxnResponse.readEntity(ReceiveResponse.class);

            assertThat(receiveResponse.getPayload()).isEqualTo(transactionData);

            restUtils.findTransaction(persistedKey, recipients).forEach(r -> {
                assertThat(r.getStatus()).isEqualTo(200);
            });

            restUtils.findTransaction(persistedKey, partyHelper.findByAlias("C"), partyHelper.findByAlias("B")).forEach(r -> {
                assertThat(r.getStatus()).isEqualTo(404);
            });
        });

        When("sender party receives transaction with no payload from Quorum peer", () -> {
            Party sender = senderHolder.stream().findAny().get();

            Response response = restUtils.sendRaw(sender, null, recipients);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(400);
        });

        When("sender party receives transaction with an unknown party from Quorum peer", () -> {
            Party sender = senderHolder.stream().findAny().get();

            final Response response = client.target(sender.getQ2TUri())
                .path("sendraw")
                .request()
                .header(SENDER, sender.getPublicKey())
                .header(RECIPIENTS, "8SjRHlUBe4hAmTk3KDeJ96RhN+s10xRrHDrxEi1O5W0=")
                .post(Entity.entity(transactionData, MediaType.APPLICATION_OCTET_STREAM));

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(400);
        });

        Then("an invalid request error is raised", () -> {
            //FIXME: validated in sending function
        });

        Then("^sender party stores the transaction$", () -> {
            Party sender = senderHolder.iterator().next();
            try (PreparedStatement statement
                = sender.getDatabaseConnection()
                    .prepareStatement("SELECT COUNT(*) FROM ENCRYPTED_TRANSACTION WHERE HASH = ?")) {
                    statement.setBytes(1, Base64.getDecoder().decode(storedHashes.iterator().next()));

                    try (ResultSet results = statement.executeQuery()) {
                        assertThat(results.next()).isTrue();
                        assertThat(results.getLong(1)).isEqualTo(1);
                    }

                }
        });

        Then("^forwards the transaction to recipient part(?:y|ies)$", () -> {

            recipients.forEach(rec -> {
                String storedHash = storedHashes.stream().findAny().get();
                Response response = restUtils.receiveRaw(storedHash, rec);

                assertThat(response.getStatus()).isEqualTo(200);
                final byte[] result = response.readEntity(byte[].class);
                assertThat(result).isEqualTo(transactionData);
            });

        });

        Then("^.*does not forward transaction to any recipients?$", () -> {
            partyHelper.getParties()
                .filter(p -> !senderHolder.contains(p))
                .forEach(p -> {
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
