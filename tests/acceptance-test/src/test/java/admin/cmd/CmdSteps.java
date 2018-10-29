package admin.cmd;

import com.quorum.tessera.config.Peer;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import com.quorum.tessera.test.RestPartyHelper;
import com.quorum.tessera.test.rest.RestUtils;
import cucumber.api.java8.En;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;

public class CmdSteps implements En {

    private final PartyHelper partyHelper = new RestPartyHelper();

    private final RestUtils restUtils = new RestUtils();

    private Client client = RestUtils.buildClient();

    public CmdSteps() {

        Party subjectNode = partyHelper.getParties().findAny().get();

        Given("any node is running", () -> {
            assertThat(Stream.of(subjectNode)
                .map(Party::getUri)
                .map(client::target)
                .map(t -> t.path("upcheck"))
                .map(WebTarget::request)
                .map(Invocation.Builder::get)
                .allMatch(r -> r.getStatus() == 200))
                .isTrue();

        });

        When("admin user executes add peer", () -> {
            int exitcode = Utils.addPeer(subjectNode, "bogus");
            assertThat(exitcode).isEqualTo(0);
        });

        Then("a peer is added to party", () -> {

            Response response = Stream.of(subjectNode)
                .map(Party::getUri)
                .map(client::target)
                .map(t -> t.path("config"))
                .map(t -> t.path("peers"))
                .map(WebTarget::request)
                .map(Invocation.Builder::get).findAny().get();

            assertThat(response.getStatus()).isEqualTo(200);
            Peer[] peers = response.readEntity(Peer[].class);

            List<String> urls = Stream.of(peers)
                .map(Peer::getUrl)
                .collect(Collectors.toList());

            assertThat(urls).contains("bogus");

        });
    }

}
