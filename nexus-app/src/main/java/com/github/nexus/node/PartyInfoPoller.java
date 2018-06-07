package com.github.nexus.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PartyInfoPoller implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoPoller.class);

    private final ScheduledExecutorService scheduledExecutorService;

    private final PartyInfoService partyInfoService;

    private final long rateInSeconds;

    private static final String PATH = "/partyinfo";

    private PartyInfoParser partyInfoParser = new PartyInfoParser() {
        @Override
        public byte[] to(PartyInfo partyInfoThing) {
            return new byte[0];
        }
    };

    public PartyInfoPoller(
            final PartyInfoService partyInfoService,
            final ScheduledExecutorService scheduledExecutorService,
            final long rateInSeconds) {
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
        Client client = ClientBuilder.newClient();
        try {

            PartyInfo partyInfo = partyInfoService.getPartyInfo();
            List<Party> parties = partyInfo.getParties();
            parties.stream().map(party -> party.getUrl())
            .forEach(url -> {
                Response response = client.target(url).path(PATH).request().get();
                byte[] encoded = response.readEntity(byte[].class);
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
