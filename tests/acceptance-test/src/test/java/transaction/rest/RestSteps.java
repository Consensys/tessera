package transaction.rest;

import com.quorum.tessera.api.model.ReceiveResponse;
import com.quorum.tessera.api.model.SendRequest;
import com.quorum.tessera.api.model.SendResponse;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.RestPartyHelper;
import cucumber.api.java8.En;

import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.quorum.tessera.test.PartyHelper;
import com.quorum.tessera.test.rest.RestUtils;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import static org.assertj.core.api.Assertions.assertThat;
import transaction.utils.Utils;

public class RestSteps implements En {


    private final RestUtils restUtils = new RestUtils();
    
    private Client client = ClientBuilder.newClient();

    private PartyHelper partyHelper = new RestPartyHelper();
    
    public RestSteps() {

        Collection<Party> senderHolder = new ArrayList<>();

        List<String> portHolder = new ArrayList<>();
        Collection<String> responseCodes = new ArrayList<>();

        Set<Party> recipients = new HashSet<>();

        Set<String> storedHashes = new TreeSet<>();

        byte[] txnData = Utils.generateTransactionData();

        Given("^Sender party (.+)$", (String pty) -> {
            senderHolder.add(partyHelper.findByAlias(pty));
        });

        Given("^Node at port (.+)$", (String port) -> portHolder.add(port));

        And("^Recipient part(?:y|ies) (.+)$", (String alias) -> {
            parseAliases(alias).stream()
                .map(partyHelper::findByAlias)
                .forEach(recipients::add);

            assertThat(recipients).isNotEmpty();
        });

        And("^all parties are running$", () -> {
            
            assertThat(partyHelper.getParties()
                .map(Party::getUri)
                .map(client::target)
                .map(t -> t.path("upcheck"))
                .map(WebTarget::request)
                .map(Invocation.Builder::get)
                .allMatch(r -> r.getStatus() == 200))
                .isTrue();
        });

        When("^a request is made against the node", () -> {
            Optional<URI> uri = Optional.of(new URI("http://localhost:" + portHolder.get(0)));

            responseCodes.add(
                uri
                    .map(client::target)
                    .map(t -> t.path("upcheck"))
                    .map(WebTarget::request)
                    .map(Invocation.Builder::get)
                    .map(Response::getStatus)
                    .get()
                    .toString()
            );
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

            Response response = restUtils.send(sender, txnData, recipients);
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

        Then("^the response code is UNAUTHORIZED", () -> {
            assertThat(responseCodes).containsExactly("401");
        });

        Then("^forwards the transaction to recipient part(?:y|ies)$", () -> {

            recipients.forEach(rec -> {
                String storedHash = storedHashes.iterator().next();
                Response response = restUtils.findTransaction(storedHash,rec)
                    .findAny().get();

                assertThat(response.getStatus()).isEqualTo(200);

                ReceiveResponse receiveResponse = response.readEntity(ReceiveResponse.class);

                assertThat(receiveResponse.getPayload()).isEqualTo(txnData);
            });

            partyHelper.getParties()
                .filter(p -> !senderHolder.contains(p))
                .filter(p -> !recipients.contains(p))
                .forEach(p -> {
                    String storedHash = storedHashes.iterator().next();
                    Response response = restUtils.findTransaction(storedHash,p)
                        .findAny().get();
                    assertThat(response.getStatus()).isEqualTo(404);
                });

        });

        Then("^.*does not forward transaction to any recipients?$", () -> {
            partyHelper.getParties()
                .filter(p -> !senderHolder.contains(p))
                .forEach(p -> {
                    String storedHash = storedHashes.stream().findAny().get();
                    Response response = restUtils.findTransaction(storedHash,p).findAny().get();
                    assertThat(response.getStatus()).isEqualTo(404);
                });

        });

        Then("an invalid request error is raised", () -> {
              //TODO: asserted in preceding step
        });
    }

    static List<String> parseAliases(String alias) {
        return Arrays.asList(alias.split(",| and "));
    }

}
