package com.quorum.tessera.p2p;

import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Party;
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

    private final NodeInfoPublisher nodeInfoPublisher;

    private final Executor executor;

    public PartyInfoPoller(final PartyInfoService partyInfoService, final NodeInfoPublisher publisher) {
        this(partyInfoService, publisher, Executors.newCachedThreadPool());
    }

    public PartyInfoPoller(final PartyInfoService partyInfoService,
                           final NodeInfoPublisher nodeInfoPublisher,
                           final Executor executor) {
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
        this.nodeInfoPublisher = Objects.requireNonNull(nodeInfoPublisher);
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

        final NodeInfo nodeInfo = partyInfoService.getPartyInfo();
        final String ourUrl = nodeInfo.getUrl();

        LOGGER.debug("Contacting following peers with PartyInfo: {}", nodeInfo.getParties());
        LOGGER.debug("Sending recipients {}", nodeInfo.getRecipients());

        nodeInfo.getParties().stream()
            .map(Party::getUrl)
            .filter(url -> !Objects.equals(ourUrl, url))
            .forEach(url -> pollSingleParty(url, nodeInfo));

        LOGGER.info("Finished PartyInfo polling round");
    }

    /**
     * Sends a request providing node information to a single target
     *
     * @param url              the target URL to call
     * @param existingNodeInfo the network info the node has currently stored
     */
    private void pollSingleParty(final String url, final NodeInfo existingNodeInfo) {
        CompletableFuture.runAsync(() -> nodeInfoPublisher.publishNodeInfo(url, existingNodeInfo), executor)
            .exceptionally(
                ex -> {
                    LOGGER.warn("Failed to connect to node {}, due to {}", url, ex.getMessage());
                    LOGGER.debug(null, ex);
                    return null;
                });
    }
}
