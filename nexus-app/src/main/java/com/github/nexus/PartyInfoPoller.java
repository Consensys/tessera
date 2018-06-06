package com.github.nexus;

import com.github.nexus.entity.PartyInfo;
import com.github.nexus.service.PartyInfoService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PartyInfoPoller implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoPoller.class);

    private final List<ScheduledFuture<?>> tasks = new ArrayList<>();

    private final ScheduledExecutorService scheduledExecutorService;

    private final PartyInfoService partyInfoService;

    private final long rateInSeconds;

    public PartyInfoPoller(
            final PartyInfoService partyInfoService,
            final ScheduledExecutorService scheduledExecutorService,
            final Long rateInSeconds) {
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
        this.scheduledExecutorService = Objects.requireNonNull(scheduledExecutorService);
        this.rateInSeconds = rateInSeconds;

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
            PartyInfo partyInfo = partyInfoService.pollPartyInfo();
            LOGGER.debug("Polled {}. PartyInfo : {}", getClass().getSimpleName(), partyInfo);
        } catch(Throwable ex) {
            LOGGER.error("Error thrown while executing poller. ",ex);
            throw ex;
        }
       
    }

}
