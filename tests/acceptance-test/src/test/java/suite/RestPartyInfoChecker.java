package suite;

import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import com.quorum.tessera.test.DefaultPartyHelper;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import jakarta.json.*;
import jakarta.json.stream.JsonGenerator;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestPartyInfoChecker implements PartyInfoChecker {

  private static final Logger LOGGER = LoggerFactory.getLogger(RestPartyInfoChecker.class);

  private PartyHelper partyHelper = new DefaultPartyHelper();

  @Override
  public boolean hasSynced() {
    LOGGER.trace("hasSynced {}", this);

    List<Party> parties = partyHelper.getParties().collect(Collectors.toList());

    Boolean[] results = new Boolean[parties.size()];

    for (int i = 0; i < parties.size(); i++) {
      Party p = parties.get(i);
      ServerConfig p2pConfig = p.getConfig().getP2PServerConfig();
      Client client = new ClientFactory().buildFrom(p2pConfig);

      LOGGER.debug("Request party info for {}. On {}", p.getAlias(), p2pConfig.getServerUri());
      Response response = client.target(p2pConfig.getServerUri()).path("partyinfo").request().get();

      LOGGER.debug("Requested party info for {} . {}", p.getAlias(), response.getStatus());
      if (response.getStatus() == 200) {
        final JsonObject result = response.readEntity(JsonObject.class);

        JsonWriterFactory jsonGeneratorFactory =
            Json.createWriterFactory(Map.of(JsonGenerator.PRETTY_PRINTING, true));
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = jsonGeneratorFactory.createWriter(stringWriter);
        try (jsonWriter) {
          jsonWriter.writeObject(result);
          LOGGER.debug("Reponse from node {} is {}", p.getAlias(), stringWriter.toString());
        }

        final JsonArray keys = result.getJsonArray("keys");
        final long contactedUrlCount =
            keys.stream()
                .map(JsonValue::asJsonObject)
                .map(o -> o.getString("url"))
                .collect(Collectors.toSet())
                .size();

        LOGGER.debug("Found {} peers of {} on {}", contactedUrlCount, parties.size(), p.getAlias());

        results[i] = (contactedUrlCount == parties.size());
      } else {
        results[i] = false;
      }
    }

    return Arrays.stream(results).allMatch(p -> p);
  }
}
