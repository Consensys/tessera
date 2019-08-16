package suite;

import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.sync.SyncRequestMessage;
import com.quorum.tessera.sync.SyncResponseMessage;
import com.quorum.tessera.test.DefaultPartyHelper;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

import java.util.stream.Collectors;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.ws.rs.core.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsocketPartyInfoChecker implements PartyInfoChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketPartyInfoChecker.class);

    private PartyHelper partyHelper = new DefaultPartyHelper();

    private final WebSocketContainer container = ContainerProvider.getWebSocketContainer();

    @Override
    public boolean hasSynced() {

        List<Party> parties = partyHelper.getParties().collect(Collectors.toList());

        List<Boolean> results = new ArrayList<>();

        BlockingQueue<SyncResponseMessage> queue = new LinkedBlockingQueue<>();

        WebsocketPartyInfoClientEndpoint endpoint = new WebsocketPartyInfoClientEndpoint(queue);

        for (Party party : parties) {

            ServerConfig p2pConfig = party.getConfig().getP2PServerConfig();
            URI uri = UriBuilder.fromUri(p2pConfig.getServerUri()).path("sync").build();

            final CompletableFuture completableFuture =
                    CompletableFuture.runAsync(
                            () -> {
                                try {

                                    SyncResponseMessage response = queue.take();
                                    PartyInfo partyInfo = response.getPartyInfo();

                                    long storedParties =
                                            partyInfo.getParties().stream()
                                                    // .filter(p -> Objects.nonNull(p.getLastContacted()))
                                                    .count();

                                    final boolean hasCompleted = parties.size() == storedParties;

                                    LOGGER.debug(
                                            "Node {}. Party info parties: Wanted: {}, actual: {}",
                                            party.getAlias(),
                                            parties.size(),
                                            storedParties);

                                    partyInfo.getParties().stream()
                                            .forEach(
                                                    p -> {
                                                        LOGGER.debug(
                                                                "Party.url: {}, Party.lastconnected: {}",
                                                                p.getUrl(),
                                                                p.getLastContacted());
                                                    });

                                    partyInfo.getRecipients().stream()
                                            .forEach(
                                                    r -> {
                                                        LOGGER.debug(
                                                                "Recipient.url: {}, Recipient.key: {}",
                                                                r.getUrl(),
                                                                r.getKey());
                                                    });

                                    results.add(hasCompleted);
                                } catch (InterruptedException ex) {
                                    LOGGER.warn(ex.getMessage());
                                }
                            });

            LOGGER.debug("Connecting to {}. ", uri);
            try (Session session = container.connectToServer(endpoint, uri)) {
                LOGGER.info("Connected to {}", uri);

                final SyncRequestMessage request =
                        SyncRequestMessage.Builder.create(SyncRequestMessage.Type.PARTY_INFO).build();

                LOGGER.debug("Sending {}", request);

                session.getBasicRemote().sendObject(request);
                LOGGER.debug("Sent {}", request);

                completableFuture.get();

            } catch (InterruptedException
                    | ExecutionException
                    | EncodeException
                    | DeploymentException
                    | IOException ex) {
                LOGGER.debug("", ex);
                return false;
            }
        }

        LOGGER.info("Results : {}", results);

        return results.stream().allMatch(b -> b);
    }
}
