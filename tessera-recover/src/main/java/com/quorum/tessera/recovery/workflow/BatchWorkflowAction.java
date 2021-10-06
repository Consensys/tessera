package com.quorum.tessera.recovery.workflow;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface BatchWorkflowAction {

  default boolean doExecute(BatchWorkflowContext context) {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    logger.trace("Enter execute {}", context);
    ZonedDateTime start = ZonedDateTime.now();
    boolean result = execute(context);
    long millisecs = start.until(ZonedDateTime.now(), ChronoUnit.MILLIS);
    logger.debug("Execution millisecs {}", millisecs);
    logger.trace("Exit execute {}. Outcome: {}", context, result);
    return result;
  }

  boolean execute(BatchWorkflowContext context);
}
