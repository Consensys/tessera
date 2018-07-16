package com.github.tessera.node;

import com.github.tessera.api.model.ApiPath;
import com.github.tessera.node.model.Party;
import com.github.tessera.node.model.PartyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.ConnectException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

public class PartyInfoPoller implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoPoller.class);

    private final ScheduledExecutorService scheduledExecutorService;

    private final PartyInfoService partyInfoService;

    private final long rateInSeconds;

    private PostDelegate postDelegate;

    private PartyInfoParser partyInfoParser;

    public PartyInfoPoller(final PartyInfoService partyInfoService,
            final ScheduledExecutorService scheduledExecutorService,
            final PartyInfoParser partyInfoParser,
            final PostDelegate postDelegate,
            final long rateInSeconds) {
        this.partyInfoService = requireNonNull(partyInfoService);
        this.scheduledExecutorService = requireNonNull(scheduledExecutorService);
        this.partyInfoParser = requireNonNull(partyInfoParser);
        this.rateInSeconds = rateInSeconds;
        this.postDelegate = requireNonNull(postDelegate);

    }

    @PostConstruct
    public void start() {
        LOGGER.info("Starting {}", getClass().getSimpleName());

        scheduledExecutorService.scheduleAtFixedRate(this, rateInSeconds, rateInSeconds, TimeUnit.SECONDS);

        LOGGER.info("Started {}", getClass().getSimpleName());
    }

    @PreDestroy
    public void stop() {
        LOGGER.info("Stopping {}", getClass().getSimpleName());
        scheduledExecutorService.shutdown();
        LOGGER.info("Stopped {}", getClass().getSimpleName());
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
