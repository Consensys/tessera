package com.quorum.tessera.recovery.workflow;

public interface BatchWorkflow extends BatchWorkflowAction {
  @Override
  boolean execute(BatchWorkflowContext context);

  long getPublishedMessageCount();
}
