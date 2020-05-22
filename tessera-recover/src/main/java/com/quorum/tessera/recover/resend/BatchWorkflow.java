package com.quorum.tessera.recover.resend;



public interface BatchWorkflow extends BatchWorkflowAction {
    @Override
    boolean execute(BatchWorkflowContext context);

    long getPublishedMessageCount();

}
