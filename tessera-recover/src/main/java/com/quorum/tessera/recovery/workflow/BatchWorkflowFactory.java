package com.quorum.tessera.recovery.workflow;

import java.util.ServiceLoader;

public interface BatchWorkflowFactory {

  BatchWorkflow create(long transactionCount);

  static BatchWorkflowFactory create() {
    return ServiceLoader.load(BatchWorkflowFactory.class).findFirst().get();
  }
}
