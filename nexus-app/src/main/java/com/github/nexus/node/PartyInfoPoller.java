package com.github.nexus.node;

import com.github.nexus.node.model.Party;
import com.github.nexus.node.model.PartyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

public class PartyInfoPoller implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoPoller.class);

    private final ScheduledExecutorService scheduledExecutorService;

    private final PartyInfoService partyInfoService;

    private final long rateInSeconds;

    private PartyInfoPostDelegate partyInfoPostDelegate;

    private PartyInfoParser partyInfoParser;

    public PartyInfoPoller(final PartyInfoService partyInfoService,
                           final ScheduledExecutorService scheduledExecutorService,
                           final PartyInfoParser partyInfoParser,
                           final PartyInfoPostDelegate partyInfoPostDelegate,
                           final long rateInSeconds) {
        this.partyInfoService = requireNonNull(partyInfoService);
        this.scheduledExecutorService = requireNonNull(scheduledExecutorService);
        this.partyInfoParser = requireNonNull(partyInfoParser);
        this.rateInSeconds = rateInSeconds;
        this.partyInfoPostDelegate = requireNonNull(partyInfoPostDelegate);

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
                .map(Party::getUrl)
                .map(url -> partyInfoPostDelegate.doPost(url, encodedPartyInfo))
                .map(partyInfoParser::from)
                .forEach(partyInfoService::updatePartyInfo);

            LOGGER.debug("Polled {}. PartyInfo : {}", getClass().getSimpleName(), partyInfo);
        } catch (Throwable ex) {
            LOGGER.error("Error thrown while executing poller. ", ex);
            throw ex;
        }

    }

}
