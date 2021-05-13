package com.quorum.tessera.recovery.workflow;

public interface BatchWorkflowAction {

  boolean execute(BatchWorkflowContext context);
}
