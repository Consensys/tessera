package com.github.nexus.node;

import com.github.nexus.api.model.ApiPath;
import com.github.nexus.node.model.Party;
import com.github.nexus.node.model.PartyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.ConnectException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

        try {

            final PartyInfo partyInfo = partyInfoService.getPartyInfo();

            final byte[] encodedPartyInfo = partyInfoParser.to(partyInfo);

            partyInfo.getParties()
                    .stream()
                    .filter(party -> !party.getUrl().equals(partyInfo.getUrl()))
                    .map(Party::getUrl)
                    .map(url -> postDelegate.doPost(url, ApiPath.PARTYINFO, encodedPartyInfo))
                    .map(partyInfoParser::from)
                    .collect(Collectors.toList())
                    .forEach(partyInfoService::updatePartyInfo);

            LOGGER.debug("Polled {}. PartyInfo : {}", getClass().getSimpleName(), partyInfo);
        } catch (Throwable ex) {

            if (ConnectException.class.isInstance(ex.getCause())) {
                LOGGER.warn("Server error {}", ex.getMessage());
                LOGGER.debug(null, ex);
            } else {
                LOGGER.error("Error thrown while executing poller. ", ex);
                throw ex;
            }
        }

    }

}
