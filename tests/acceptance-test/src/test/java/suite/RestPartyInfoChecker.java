package suite;

import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import com.quorum.tessera.test.DefaultPartyHelper;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestPartyInfoChecker implements PartyInfoChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestPartyInfoChecker.class);

    private PartyHelper partyHelper = new DefaultPartyHelper();

    @Override
    public boolean hasSynced() {
        LOGGER.info("hasSynced {}", this);

        List<Party> parties = partyHelper.getParties().collect(Collectors.toList());

        Boolean[] results = new Boolean[parties.size()];

        for (int i = 0; i < parties.size(); i++) {
            Party p = parties.get(i);
            ServerConfig p2pConfig = p.getConfig().getP2PServerConfig();
            Client client = new ClientFactory().buildFrom(p2pConfig);

            LOGGER.debug("Request party info for {}. On {}", p.getAlias(), p2pConfig.getServerUri());
            Response response = client.target(p2pConfig.getServerUri())
                    .path("partyinfo")
                    .request()
                    .get();

            LOGGER.debug("Requested party info for {} . {}", p.getAlias(), response.getStatus());
            if (response.getStatus() == 200) {
                final JsonObject result = response.readEntity(JsonObject.class);
                final int peerCount = (int)
                    result.getJsonArray("peers")
                        .stream()
                        .map(val -> (JsonObject) val)
                        .filter(peer -> !peer.isNull("lastContact"))
                        .count();

                LOGGER.debug("Found {} peers of {} on {}", peerCount,parties.size(), p.getAlias());
                results[i] = peerCount == parties.size();
            } else {
                results[i] = false;
            }
        }

        return Arrays.stream(results).allMatch(p -> p);
    }

}
