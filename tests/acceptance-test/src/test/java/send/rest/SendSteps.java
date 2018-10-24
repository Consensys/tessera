package send.rest;

import com.quorum.tessera.api.model.ReceiveResponse;
import com.quorum.tessera.api.model.SendRequest;
import com.quorum.tessera.api.model.SendResponse;
import com.quorum.tessera.test.ClientFacade;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyFactory;
import com.quorum.tessera.test.RestPartyFactory;
import cucumber.api.java8.En;
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
import javax.json.Json;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;

public class SendSteps implements En {

    private ClientFacade clientFacade = ClientFacade.create(ClientFacade.CommunicationType.REST);

    private Client client = ClientBuilder.newClient();

    private PartyFactory partyFactory = new RestPartyFactory();
    
    public SendSteps() {

        Collection<Party> senderHolder = new ArrayList<>();

        Set<Party> recipients = new HashSet<>();

        Set<String> storedHashes = new TreeSet<>();

        byte[] txnData = ClientFacade.generateTransactionData();

        Given("^Sender party (.+)$", (String pty) -> {
            senderHolder.add(partyFactory.findByAlias(pty));
        });

        And("^Recipient part(?:y|ies) (.+)$", (String alias) -> {
            parseAliases(alias).stream()
                .map(partyFactory::findByAlias)
                .forEach(recipients::add);

            assertThat(recipients).isNotEmpty();
        });

        And("^all parties are running$", () -> {
            assertThat(partyFactory.getParties()
                .allMatch(clientFacade::isUp))
                .isTrue();
        });

        When("sender party receives transaction with an unknown party from Quorum peer", () -> {
            Party sender = senderHolder.stream().findAny().get();

            final SendRequest sendRequest = new SendRequest();
            sendRequest.setFrom(sender.getPublicKey());
            sendRequest.setTo("8SjRHlUBe4hAmTk3KDeJ96RhN+s10xRrHDrxEi1O5W0=");
            sendRequest.setPayload(txnData);

            final Response response = client.target(sender.getUri())
                .path("send")
                .request()
                .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(400);
        });

        When("^sender party receives transaction with no payload from Quorum peer$", () -> {

            Party sender = senderHolder.stream().findAny().get();

            List<String> recipientKeys = recipients.stream()
                .map(Party::getPublicKey)
                .collect(Collectors.toList());

            final String sendRequest = Json.createObjectBuilder()
                .add("from", sender.getPublicKey())
                .add("to",
                    Json.createArrayBuilder(recipientKeys)
                )
                .build().toString();

            Response response = client.target(sender.getUri()).path("send")
                .request()
                .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

            assertThat(response.getStatus()).isEqualTo(400);

        });

        When("^sender party receives transaction with no sender key defined from Quorum peer$", () -> {
            Party sender = senderHolder.stream().findAny().get();
            SendRequest sendRequest = new SendRequest();

            sendRequest.setPayload(txnData);

            String[] recipientArray = recipients.stream()
                .map(Party::getPublicKey)
                .collect(Collectors.toList())
                .toArray(new String[recipients.size()]);

            sendRequest.setTo(recipientArray);

            Response response = client.target(sender.getUri()).path("send")
                .request().post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

            assertThat(response.getStatus()).isEqualTo(201);

            SendResponse sendResponse = response.readEntity(SendResponse.class);

            storedHashes.add(sendResponse.getKey());

        });

        When("^sender party receives transaction from Quorum peer$", () -> {

            Party sender = senderHolder.stream().findAny().get();

            Response response = clientFacade.send(sender, txnData, recipients.toArray(new Party[recipients.size()]));

            assertThat(response.getStatus()).isEqualTo(201);

            SendResponse sendResponse = response.readEntity(SendResponse.class);

            storedHashes.add(sendResponse.getKey());

        });

        Then("^sender party stores the transaction$", () -> {
            Party sender = senderHolder.iterator().next();
            try (PreparedStatement statement
                = sender.getDatabaseConnection().prepareStatement("SELECT COUNT(*) FROM ENCRYPTED_TRANSACTION WHERE HASH = ?")) {
                statement.setBytes(1, Base64.getDecoder().decode(storedHashes.iterator().next()));

                try (ResultSet results = statement.executeQuery()) {
                    assertThat(results.next()).isTrue();
                    assertThat(results.getLong(1)).isEqualTo(1);
                }

            }
        });

        Then("^forwards the transaction to recipient part(?:y|ies)$", () -> {

            recipients.forEach(rec -> {
                String storedHash = storedHashes.iterator().next();
                Response response = clientFacade.find(rec, storedHash);

                assertThat(response.getStatus()).isEqualTo(200);

                ReceiveResponse receiveResponse = response.readEntity(ReceiveResponse.class);

                assertThat(receiveResponse.getPayload()).isEqualTo(txnData);
            });

            partyFactory.getParties()
                .filter(p -> !senderHolder.contains(p))
                .filter(p -> !recipients.contains(p))
                .forEach(p -> {
                    String storedHash = storedHashes.iterator().next();
                    Response response = clientFacade.find(p, storedHash);
                    assertThat(response.getStatus()).isEqualTo(404);
                });

        });

        Then("^.*does not forward transaction to any recipients?$", () -> {
            partyFactory.getParties()
                .filter(p -> !senderHolder.contains(p))
                .forEach(p -> {
                    String storedHash = storedHashes.stream().findAny().get();
                    Response response = clientFacade.find(p, storedHash);
                    assertThat(response.getStatus()).isEqualTo(404);
                });

        });

        Then("an invalid request error is raised", () -> {

        });
    }

    static List<String> parseAliases(String alias) {
        return Arrays.asList(alias.split(",| and "));
    }

}
