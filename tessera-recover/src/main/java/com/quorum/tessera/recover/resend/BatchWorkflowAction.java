package com.quorum.tessera.recover.resend;

public interface BatchWorkflowAction {

    boolean execute(BatchWorkflowContext context);

}
