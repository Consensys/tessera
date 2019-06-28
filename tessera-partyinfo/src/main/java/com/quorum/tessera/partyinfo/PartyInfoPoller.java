package com.quorum.tessera.partyinfo;

import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.util.Objects;

/**
 * Polls every so often to all known nodes for any new discoverable nodes This
 * keeps all nodes up-to date and discoverable by other nodes
 */
public class PartyInfoPoller implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoPoller.class);

    private final PartyInfoService partyInfoService;

    private final PartyInfoParser partyInfoParser;

    private final P2pClient p2pClient;

    public PartyInfoPoller(final PartyInfoService partyInfoService,
                           final PartyInfoParser partyInfoParser,
                           final P2pClient p2pClient) {
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
        this.partyInfoParser = Objects.requireNonNull(partyInfoParser);
        this.p2pClient = Objects.requireNonNull(p2pClient);
    }

    /**
     * Iterates over all known parties and contacts them for the current state
     * of their known node discovery list
     * <p>
     * For Tessera 0.9 backwards, after contacting the known parties,
     * this poller then updates this nodes list of data with any new information collected.
     *
     * This behaviour is now deprecated since the /partyinfo API call now has been made more strict
     * with node validation to prevent exploiting the API to attack the Tessera network.
     *
     * This call is merely to let its parties know about this node existence,
     * any recipients that want to be added to this node's PartyInfo will need to make their own partyinfo call
     * and validation
     */
    @Override
    public void run() {
        LOGGER.debug("Polling {}", getClass().getSimpleName());

        final PartyInfo partyInfo = partyInfoService.getPartyInfo();

        final byte[] encodedPartyInfo = partyInfoParser.to(partyInfo);

        partyInfo
            .getParties()
            .stream()
            .filter(party -> !party.getUrl().equals(partyInfo.getUrl()))
            .map(Party::getUrl)
            .forEach(url -> pollSingleParty(url, encodedPartyInfo));

        LOGGER.debug("Polled {}. PartyInfo : {}", getClass().getSimpleName(), partyInfo);
    }

    /**
     * Sends a request for node information to a single target
     *
     * @param url              the target URL to call
     * @param encodedPartyInfo the encoded current party information
     */
    private void pollSingleParty(final String url, final byte[] encodedPartyInfo) {

        try {
            p2pClient.sendPartyInfo(url, encodedPartyInfo);
        } catch (final Exception ex) {

            if (ConnectException.class.isInstance(ex.getCause())) {
                LOGGER.warn("Server error {} when connecting to {}", ex.getMessage(), url);
                LOGGER.debug(null, ex);
            } else {
                LOGGER.error("Error {} while executing poller for {}. ", ex.getMessage(), url);
                LOGGER.debug(null, ex);
                throw ex;
            }

        }

    }

}
