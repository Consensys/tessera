package com.quorum.tessera.recovery.workflow;

public interface BatchWorkflowFilter extends BatchWorkflowAction {

  @Override
  default boolean execute(BatchWorkflowContext context) {
    return filter(context);
  }

  boolean filter(BatchWorkflowContext context);
}
