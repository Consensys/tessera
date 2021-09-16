package com.quorum.tessera.threading;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Schedules a continuous running task as an alternative to a {@link Thread} running a {@code
 * while(true)} loop
 *
 * <p>Also allows delays if required between each execution of the loop
 */
public class TesseraScheduledExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(TesseraScheduledExecutor.class);

  private final ScheduledExecutorService executor;

  private final Runnable action;

  private final long rate;

  private final long initialDelay;

  public TesseraScheduledExecutor(
      final ScheduledExecutorService executor,
      final Runnable action,
      final long rate,
      final long delay) {
    this.executor = Objects.requireNonNull(executor);
    this.action = Objects.requireNonNull(action);
    this.rate = rate;
    this.initialDelay = delay;
  }

  /**
   * Starts the submitted task and schedules it to run every given time frame. Catches any Throwable
   * and logs it so that the scheduling doesn't break
   */
  @PostConstruct
  public void start() {
    LOGGER.info("Starting {}", this.action.getClass().getSimpleName());

    final Runnable exceptionSafeRunnable =
        () -> {
          try {
            LOGGER.debug("{} has started running", action.getClass().getSimpleName());

            this.action.run();
          } catch (final Throwable ex) {
            LOGGER.error(
                "Error when executing action {}, exception details:",
                action.getClass().getSimpleName(),
                ex);
          } finally {
            LOGGER.debug("{} has finished running", action.getClass().getSimpleName());
          }
        };

    this.executor.scheduleWithFixedDelay(
        exceptionSafeRunnable, initialDelay, rate, TimeUnit.MILLISECONDS);

    LOGGER.info("Started {}", this.action.getClass().getSimpleName());
  }

  /**
   * Stops any more executions of the submitted task from running. Does not cancel the currently
   * running task, which may be blocking
   */
  @PreDestroy
  public void stop() {
    LOGGER.info("Stopping {}", this.action.getClass().getSimpleName());

    this.executor.shutdown();

    LOGGER.info("Stopped {}", this.action.getClass().getSimpleName());
  }
}
