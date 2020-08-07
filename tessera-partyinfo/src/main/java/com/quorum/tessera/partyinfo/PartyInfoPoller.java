package com.quorum.tessera.partyinfo;

import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Polls every so often to all known nodes for any new discoverable nodes. This keeps all nodes up-to date and
 * discoverable by other nodes
 */
public class PartyInfoPoller implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoPoller.class);

    private final PartyInfoService partyInfoService;

    private final PartyInfoParser partyInfoParser;

    private final P2pClient p2pClient;

    private final Executor executor;

    public PartyInfoPoller(final PartyInfoService partyInfoService, final P2pClient p2pClient) {
        this(partyInfoService, PartyInfoParser.create(), p2pClient, Executors.newCachedThreadPool());
    }

    public PartyInfoPoller(
            final PartyInfoService partyInfoService,
            final PartyInfoParser partyInfoParser,
            final P2pClient p2pClient,
            final Executor executor) {
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
        this.partyInfoParser = Objects.requireNonNull(partyInfoParser);
        this.p2pClient = Objects.requireNonNull(p2pClient);
        this.executor = Objects.requireNonNull(executor);
    }

    /**
     * Iterates over all known parties and contacts them for the current state of their known node discovery list
     *
     * <p>For Tessera 0.9 backwards, after contacting the known parties, this poller then updates this nodes list of
     * data with any new information collected.
     *
     * <p>This behaviour is now deprecated since the /partyinfo API call now has been made more strict with node
     * validation to prevent exploiting the API to attack the Tessera network.
     *
     * <p>This call is merely to let its parties know about this node existence, any recipients that want to be added to
     * this node's PartyInfo will need to make their own partyinfo call and validation
     */
    @Override
    public void run() {
        LOGGER.info("Started PartyInfo polling round");

        final PartyInfo partyInfo = PartyInfo.from(partyInfoService.getPartyInfo());
        final byte[] encodedPartyInfo = partyInfoParser.to(partyInfo);

        final String ourUrl = partyInfo.getUrl();

        LOGGER.debug("Contacting following peers with PartyInfo: {}", partyInfo.getParties());
        LOGGER.debug("Sending recipients {}", partyInfo.getRecipients());

        partyInfo.getParties().stream()
                .map(Party::getUrl)
                .filter(url -> !ourUrl.equals(url))
                .forEach(url -> pollSingleParty(url, encodedPartyInfo));

        LOGGER.info("Finished PartyInfo polling round");
    }

    /**
     * Sends a request for node information to a single target
     *
     * @param url the target URL to call
     * @param encodedPartyInfo the encoded current party information
     */
    private void pollSingleParty(final String url, final byte[] encodedPartyInfo) {
        CompletableFuture.runAsync(() -> p2pClient.sendPartyInfo(url, encodedPartyInfo), executor)
                .exceptionally(
                        ex -> {
                            LOGGER.warn("Failed to connect to node {}, due to {}", url, ex.getMessage());
                            LOGGER.debug(null, ex);
                            return null;
                        });
    }
}
