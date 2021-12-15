package com.quorum.tessera.p2p.partyinfo;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.discovery.NodeUri;
import com.quorum.tessera.partyinfo.P2pClient;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.PartyInfoBuilder;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import jakarta.ws.rs.ProcessingException;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Polls every so often to all known nodes for any new discoverable nodes. This keeps all nodes
 * up-to date and discoverable by other nodes
 */
public class PartyInfoBroadcaster implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoBroadcaster.class);

  private final Discovery discovery;

  private final PartyInfoParser partyInfoParser;

  private final P2pClient p2pClient;

  private final Executor executor;

  private final PartyStore partyStore;

  public PartyInfoBroadcaster(final P2pClient p2pClient) {
    this(
        Discovery.create(),
        PartyInfoParser.create(),
        p2pClient,
        Executors.newCachedThreadPool(),
        PartyStore.getInstance());
  }

  public PartyInfoBroadcaster(
      final Discovery discovery,
      final PartyInfoParser partyInfoParser,
      final P2pClient p2pClient,
      final Executor executor,
      final PartyStore partyStore) {
    this.discovery = Objects.requireNonNull(discovery);
    this.partyInfoParser = Objects.requireNonNull(partyInfoParser);
    this.p2pClient = Objects.requireNonNull(p2pClient);
    this.executor = Objects.requireNonNull(executor);
    this.partyStore = Objects.requireNonNull(partyStore);
  }

  /**
   * Iterates over all known parties and contacts them for the current state of their known node
   * discovery list
   *
   * <p>For Tessera 0.9 backwards, after contacting the known parties, this poller then updates this
   * nodes list of data with any new information collected.
   *
   * <p>This behaviour is now deprecated since the /partyinfo API call now has been made more strict
   * with node validation to prevent exploiting the API to attack the Tessera network.
   *
   * <p>This call is merely to let its parties know about this node existence, any recipients that
   * want to be added to this node's PartyInfo will need to make their own partyinfo call and
   * validation
   */
  @Override
  public void run() {
    LOGGER.info("Started PartyInfo polling round");

    partyStore.loadFromConfigIfEmpty();

    final NodeInfo nodeInfo = discovery.getCurrent();

    final NodeUri ourUrl = NodeUri.create(nodeInfo.getUrl());

    final PartyInfo partyInfo =
        PartyInfoBuilder.create()
            .withUri(nodeInfo.getUrl())
            .withRecipients(nodeInfo.getRecipientsAsMap())
            .build();

    final byte[] encodedPartyInfo = partyInfoParser.to(partyInfo);

    LOGGER.debug("Contacting following peers with PartyInfo: {}", partyInfo.getParties());

    LOGGER.debug("Sending party info {}", nodeInfo);
    partyStore.getParties().stream()
        .map(NodeUri::create)
        .filter(url -> !ourUrl.equals(url))
        .forEach(url -> pollSingleParty(url.asString(), encodedPartyInfo));

    LOGGER.info("Finished PartyInfo polling round");
  }

  /**
   * Sends a request for node information to a single target
   *
   * @param url the target URL to call
   * @param encodedPartyInfo the encoded current party information
   */
  protected void pollSingleParty(final String url, final byte[] encodedPartyInfo) {
    final NodeUri nodeUri = NodeUri.create(url);
    CompletableFuture.runAsync(
            () -> {
              LOGGER.debug("Sending party info to {}", nodeUri.asString());
              p2pClient.sendPartyInfo(url, encodedPartyInfo);
              LOGGER.debug("Sent party info to {}", nodeUri.asString());
            },
            executor)
        .exceptionally(
            ex -> {
              Throwable cause = Optional.of(ex).map(Throwable::getCause).orElse(ex);

              LOGGER.warn("Failed to connect to node {}, due to {}", url, cause.getMessage());
              LOGGER.debug("Send failure exception", cause);
              if (ProcessingException.class.isInstance(cause)) {
                discovery.onDisconnect(URI.create(url));
                partyStore.remove(URI.create(url));
              }
              return null;
            });
  }
}
