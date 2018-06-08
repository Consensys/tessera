package com.github.nexus.node;

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

    private static final String PATH = "/partyinfo";

    private  PartyInfoPostDelegate partyInfoPostDelegate;

    private PartyInfoParser partyInfoParser;

    public PartyInfoPoller(
            final PartyInfoService partyInfoService,
            final ScheduledExecutorService scheduledExecutorService,
            final PartyInfoParser partyInfoParser,
            PartyInfoPostDelegate partyInfoPostDelegate,
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

            PartyInfo partyInfo = partyInfoService.getPartyInfo();
            byte[] encodedPartyInfo = partyInfoParser.to(partyInfo);

            partyInfo.getParties().stream()
                .map(party -> party.getUrl())
            .forEach(url -> {
                byte[] encoded =  partyInfoPostDelegate.doPost(url,encodedPartyInfo);
                PartyInfo updatedPartyInfo = partyInfoParser.from(encoded);
                partyInfoService.updatePartyInfo(updatedPartyInfo);
            });


            LOGGER.debug("Polled {}. PartyInfo : {}", getClass().getSimpleName(), partyInfo);
        } catch(Throwable ex) {
            LOGGER.error("Error thrown while executing poller. ",ex);
            throw ex;
        }
       
    }

}
