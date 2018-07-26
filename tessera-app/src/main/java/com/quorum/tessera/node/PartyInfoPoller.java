package com.quorum.tessera.node;

import com.quorum.tessera.api.model.ApiPath;
import com.quorum.tessera.node.model.Party;
import com.quorum.tessera.node.model.PartyInfo;
import com.quorum.tessera.sync.ResendPartyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.util.Objects;
import java.util.Set;

/**
 * Polls every so often to all known nodes for any new discoverable nodes
 * This keeps all nodes up-to date and discoverable by other nodes
 */
public class PartyInfoPoller implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoPoller.class);

    private final PartyInfoService partyInfoService;

    private final PostDelegate postDelegate;

    private final PartyInfoParser partyInfoParser;

    private final ResendPartyStore resendPartyStore;

    public PartyInfoPoller(final PartyInfoService partyInfoService,
                           final PartyInfoParser partyInfoParser,
                           final PostDelegate postDelegate,
                           final ResendPartyStore resendPartyStore) {
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
        this.partyInfoParser = Objects.requireNonNull(partyInfoParser);
        this.postDelegate = Objects.requireNonNull(postDelegate);
        this.resendPartyStore = Objects.requireNonNull(resendPartyStore);
    }

    /**
     * Iterates over all known parties and contacts them for the current state
     * of their known node discovery list
     *
     * It then updates this nodes list of data with any new information collected
     */
    @Override
    public void run() {
        LOGGER.debug("Polling {}", getClass().getSimpleName());

        final PartyInfo partyInfo = partyInfoService.getPartyInfo();

        final byte[] encodedPartyInfo = partyInfoParser.to(partyInfo);

        partyInfo
            .getParties()
            .parallelStream()
            .filter(party -> !party.getUrl().equals(partyInfo.getUrl()))
            .map(Party::getUrl)
            .map(url -> pollSingleParty(url, encodedPartyInfo))
            .filter(Objects::nonNull)
            .map(partyInfoParser::from)
            .forEach(newPartyInfo -> {
                this.resendForNewKeys(newPartyInfo);
                partyInfoService.updatePartyInfo(newPartyInfo);
            });

        LOGGER.debug("Polled {}. PartyInfo : {}", getClass().getSimpleName(), partyInfo);
    }

    /**
     * Sends a request for node information to a single target
     * If it cannot connect to the target, it returns null, otherwise throws
     * any exception that can be thrown from {@link javax.ws.rs.client.Client}
     *
     * @param url              the target URL to call
     * @param encodedPartyInfo the encoded current party information
     * @return the encoded partyinfo from the target node, or null is the node
     * could not be reached
     */
    private byte[] pollSingleParty(final String url, final byte[] encodedPartyInfo) {

        try {
            return postDelegate.doPost(url, ApiPath.PARTYINFO, encodedPartyInfo);

        } catch (final Exception ex) {

            if (ConnectException.class.isInstance(ex.getCause())) {
                LOGGER.warn("Server error {} when connecting to {}", ex.getMessage(), url);
                LOGGER.debug(null, ex);
                return null;
            } else {
                LOGGER.error("Error thrown while executing poller. ", ex);
                throw ex;
            }

        }

    }

    private void resendForNewKeys(final PartyInfo receivedPartyInfo) {
        final Set<Party> newPartiesFound = this.partyInfoService.findUnsavedParties(receivedPartyInfo);
        this.resendPartyStore.addUnseenParties(newPartiesFound);
    }

}
