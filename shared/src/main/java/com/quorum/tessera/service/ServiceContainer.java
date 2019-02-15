package com.quorum.tessera.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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

    public void start() {
        executorService.scheduleAtFixedRate(this, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        executorService.shutdown();
        service.stop();
    }

    @Override
    public void run() {
        if (service.status() == Service.Status.STOPPED) {
            try {
                service.start();
            } catch (Throwable ex) {
                LOGGER.warn(null, ex.getCause());
            }
        }
    }
    
}
