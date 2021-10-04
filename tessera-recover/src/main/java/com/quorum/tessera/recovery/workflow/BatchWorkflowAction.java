package com.quorum.tessera.recovery.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface BatchWorkflowAction {

  default boolean doExecute(BatchWorkflowContext context) {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    logger.debug("Enter execute {}", context);
    boolean result = execute(context);
    logger.debug("Exit execute {}. Outcome: {}", context, result);
    return result;
  }

  boolean execute(BatchWorkflowContext context);
}
