package com.github.tessera.node;

import com.github.tessera.api.model.ApiPath;
import com.github.tessera.node.model.Party;
import com.github.tessera.node.model.PartyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.util.Objects;

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

        partyInfo
            .getParties()
            .parallelStream()
            .filter(party -> !party.getUrl().equals(partyInfo.getUrl()))
            .map(Party::getUrl)
            .map(url -> pollSingleParty(url, encodedPartyInfo))
            .filter(Objects::nonNull)
            .map(partyInfoParser::from)
            .forEach(partyInfoService::updatePartyInfo);

        LOGGER.debug("Polled {}. PartyInfo : {}", getClass().getSimpleName(), partyInfo);
    }

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
}
