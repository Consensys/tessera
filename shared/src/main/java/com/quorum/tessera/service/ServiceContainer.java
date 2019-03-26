package com.quorum.tessera.service;

import com.quorum.tessera.service.Service.Status;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceContainer implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceContainer.class);

    private Service service;

    private ScheduledExecutorService executorService;

    private long initialDelay;

    private long period;

    public ServiceContainer(Service service) {
        this(service, Executors.newScheduledThreadPool(1), 1000L, 1000L);
    }

    public ServiceContainer(Service service,
            ScheduledExecutorService executorService,
            long initialDelay, long period) {
        this.service = service;
        this.executorService = executorService;
        this.initialDelay = initialDelay;
        this.period = period;
    }

    @PostConstruct
    public void start() {
        executorService.scheduleAtFixedRate(this, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void stop() {
        executorService.shutdown();
        service.stop();
    }

    @Override
    public void run() {
        LOGGER.trace("Check status {}",service);
        Status status = service.status();
        LOGGER.trace("{} Status is {}",service,status);

        if (status == Service.Status.STOPPED) {
            LOGGER.warn("Service {} not stopped attempt to restart.",service);
            try {
                LOGGER.debug("Starting service {}",service);
                service.start();
                LOGGER.debug("Started service {}",service);
            } catch (Throwable ex) {
                LOGGER.trace(null, ex);
                LOGGER.warn("Exception thrown : {} While starting service {}",
                        Optional.ofNullable(ex.getCause()).orElse(ex).getMessage(),service);
            }
        }
    }
    
}
