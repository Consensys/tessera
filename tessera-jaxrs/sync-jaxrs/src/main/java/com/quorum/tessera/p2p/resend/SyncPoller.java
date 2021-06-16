package com.quorum.tessera.p2p.resend;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.p2p.partyinfo.PartyInfoParser;
import com.quorum.tessera.partyinfo.P2pClient;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.PartyInfoBuilder;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A poller that will contact all outstanding parties that need to have transactions resent for a
 * single round
 */
public class SyncPoller implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(SyncPoller.class);

  private final ExecutorService executorService;

  private final ResendPartyStore resendPartyStore;

  private final TransactionRequester transactionRequester;

  private final Discovery discovery;

  private final P2pClient p2pClient;

  private final PartyInfoParser partyInfoParser;

  public SyncPoller(
      ResendPartyStore resendPartyStore,
      TransactionRequester transactionRequester,
      P2pClient p2pClient) {

    this(
        Executors.newCachedThreadPool(),
        resendPartyStore,
        transactionRequester,
        Discovery.create(),
        PartyInfoParser.create(),
        p2pClient);
  }

  public SyncPoller(
      final ExecutorService executorService,
      final ResendPartyStore resendPartyStore,
      final TransactionRequester transactionRequester,
      final Discovery discovery,
      final PartyInfoParser partyInfoParser,
      final P2pClient p2pClient) {
    this.executorService = Objects.requireNonNull(executorService);
    this.resendPartyStore = Objects.requireNonNull(resendPartyStore);
    this.transactionRequester = Objects.requireNonNull(transactionRequester);
    this.discovery = Objects.requireNonNull(discovery);
    this.partyInfoParser = Objects.requireNonNull(partyInfoParser);
    this.p2pClient = Objects.requireNonNull(p2pClient);
  }

  /**
   * Retrieves all of the outstanding parties and makes an attempt to make the resend request
   * asynchronously. If the request fails then the party is submitted back to the store for a later
   * attempt.
   */
  @Override
  public void run() {

    NodeInfo currentNodeInfo = discovery.getCurrent();

    final PartyInfo partyInfo =
        PartyInfoBuilder.create()
            .withUri(currentNodeInfo.getUrl())
            .withRecipients(currentNodeInfo.getRecipientsAsMap())
            .build();

    final Set<Party> unseenParties =
        partyInfo.getParties().stream()
            .filter(p -> !p.getUrl().equals(partyInfo.getUrl()))
            .collect(Collectors.toSet());
    LOGGER.debug("Unseen parties {}", unseenParties);
    this.resendPartyStore.addUnseenParties(unseenParties);

    Optional<SyncableParty> nextPartyToSend = this.resendPartyStore.getNextParty();

    while (nextPartyToSend.isPresent()) {

      final SyncableParty requestDetails = nextPartyToSend.get();
      final String url = requestDetails.getParty().getUrl();

      final Runnable action =
          () -> {

            // perform a sendPartyInfo in order to ensure that the target tessera has the current
            // tessera as
            // a recipient
            boolean allSucceeded = updatePartyInfo(url);

            if (allSucceeded) {
              allSucceeded = this.transactionRequester.requestAllTransactionsFromNode(url);
            }

            if (!allSucceeded) {
              this.resendPartyStore.incrementFailedAttempt(requestDetails);
            }
          };

      this.executorService.submit(action);

      nextPartyToSend = this.resendPartyStore.getNextParty();
    }
  }

  private boolean updatePartyInfo(String url) {
    try {
      final NodeInfo nodeInfo = discovery.getCurrent();

      final PartyInfo partyInfo =
          PartyInfoBuilder.create()
              .withUri(nodeInfo.getUrl())
              .withRecipients(nodeInfo.getRecipientsAsMap())
              .build();

      LOGGER.debug("Sending node info {} to {}", nodeInfo, url);

      final byte[] encodedPartyInfo = partyInfoParser.to(partyInfo);

      // we deliberately discard the response as we do not want to fully duplicate the
      // PartyInfoPoller
      boolean outcome = p2pClient.sendPartyInfo(url, encodedPartyInfo);
      LOGGER.debug("Sent node info {} to {}", nodeInfo, url);
      return outcome;
    } catch (final Exception ex) {
      LOGGER.warn("Failed to connect to node {} for partyinfo, due to {}", url, ex.getMessage());
      LOGGER.debug(null, ex);
      return false;
    }
  }
}
