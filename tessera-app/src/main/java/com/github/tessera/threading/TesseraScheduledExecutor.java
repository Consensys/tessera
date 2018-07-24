package com.github.tessera.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Schedules a continuous running task as an alternative to
 * a {@link Thread} running a {@code while(true)} loop
 *
 * Also allows delays if required between each execution of the loop
 */
public class TesseraScheduledExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TesseraScheduledExecutor.class);

    private final ScheduledExecutorService executor;

    private final Runnable action;

    private final long rateInSeconds;

    public TesseraScheduledExecutor(final ScheduledExecutorService executor,
                                    final Runnable action,
                                    final long rateInSeconds) {
        this.executor = Objects.requireNonNull(executor);
        this.action = Objects.requireNonNull(action);
        this.rateInSeconds = rateInSeconds;
    }

    /**
     * Starts the submitted task and schedules it to run every given time frame
     * Catches any Throwable and logs it so that the scheduling doesn't break
     */
    @PostConstruct
    public void start() {
        LOGGER.info("Starting {}", getClass().getSimpleName());

        final Runnable exceptionSafeRunnable = () -> {
            try {
                action.run();
            } catch (final Throwable ex) {
                LOGGER.error("Error when executing action {}", action.getClass().getSimpleName());
                LOGGER.error("Error when executing action", ex);
            }
        };

        this.executor.scheduleWithFixedDelay(exceptionSafeRunnable, rateInSeconds, rateInSeconds, TimeUnit.SECONDS);

        LOGGER.info("Started {}", getClass().getSimpleName());
    }

    /**
     * Stops any more executions of the submitted task from running
     * Does not cancel the currently running task, which may be blocking
     */
    @PreDestroy
    public void stop() {
        LOGGER.info("Stopping {}", getClass().getSimpleName());

        this.executor.shutdown();

        LOGGER.info("Stopped {}", getClass().getSimpleName());
    }

}
