package com.quorum.tessera.service;

import com.quorum.tessera.service.Service.Status;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceContainer implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceContainer.class);

  private final Service service;

  private final ScheduledExecutorService executorService;

  private final long initialDelay;

  private final long period;

  public ServiceContainer(final Service service) {
    this(service, Executors.newScheduledThreadPool(1), 1000L, 1000L);
  }

  public ServiceContainer(
      final Service service,
      final ScheduledExecutorService executorService,
      final long initialDelay,
      final long period) {
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
    LOGGER.trace("Check status {}", service);
    Status status = service.status();
    LOGGER.trace("{} Status is {}", service, status);

    if (status == Service.Status.STOPPED) {
      LOGGER.warn("Service {} is stopped, attempting to start it.", service);
      try {
        LOGGER.debug("Starting service {}", service);
        service.start();
        LOGGER.debug("Started service {}", service);
      } catch (Throwable ex) {
        LOGGER.trace(null, ex);
        LOGGER.error(
            "Exception thrown : {} While starting service {}",
            Optional.ofNullable(ex.getCause()).orElse(ex).getMessage(),
            service);
      }
    }
  }
}
