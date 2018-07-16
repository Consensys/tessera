package com.github.tessera.node;

import com.github.tessera.api.model.ApiPath;
import com.github.tessera.node.model.Party;
import com.github.tessera.node.model.PartyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PartyInfoPoller implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoPoller.class);

    private final PartyInfoService partyInfoService;

    private final PostDelegate postDelegate;

    private final PartyInfoParser partyInfoParser;

    public PartyInfoPoller(final PartyInfoService partyInfoService,
                           final PartyInfoParser partyInfoParser,
                           final PostDelegate postDelegate) {
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
        this.partyInfoParser = Objects.requireNonNull(partyInfoParser);
        this.postDelegate = Objects.requireNonNull(postDelegate);
    }

    @Override
    public void run() {
        LOGGER.debug("Polling {}", getClass().getSimpleName());

        final PartyInfo partyInfo = partyInfoService.getPartyInfo();

        final byte[] encodedPartyInfo = partyInfoParser.to(partyInfo);

        final Set<Party> partySet = new HashSet<>(partyInfo.getParties());

        partySet.stream()
            .filter(party -> !party.getUrl().equals(partyInfo.getUrl()))
            .map(Party::getUrl)
            .forEach(url -> pollSingleParty(url, encodedPartyInfo));

        LOGGER.debug("Polled {}. PartyInfo : {}", getClass().getSimpleName(), partyInfo);
    }

    private void pollSingleParty(final String url, final byte[] encodedPartyInfo) {
        try {
            byte[] response = postDelegate.doPost(url, ApiPath.PARTYINFO, encodedPartyInfo);
            if (null != response) {
                PartyInfo partyInfo = partyInfoParser.from(response);
                partyInfoService.updatePartyInfo(partyInfo);
            }

        } catch (Throwable ex) {

            if (ConnectException.class.isInstance(ex.getCause())) {
                LOGGER.warn("Server error {} when connecting to {}", ex.getMessage(), url);
                LOGGER.debug(null, ex);
            } else {
                LOGGER.error("Error thrown while executing poller. ", ex);
                throw ex;
            }
        }
    }

}
