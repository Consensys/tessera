package com.quorum.tessera.recover.resend;

public interface BatchWorkflowFilter extends BatchWorkflowAction {

    @Override
    default boolean execute(BatchWorkflowContext context) {
        return filter(context);
    }

    boolean filter(BatchWorkflowContext context);
}
