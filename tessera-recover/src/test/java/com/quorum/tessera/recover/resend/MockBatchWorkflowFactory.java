package com.quorum.tessera.recover.resend;


import static org.mockito.Mockito.mock;

public class MockBatchWorkflowFactory implements BatchWorkflowFactory {

    private static final ThreadLocal<BatchWorkflow> WORKFLOW = ThreadLocal.withInitial(() -> mock(BatchWorkflow.class));

    @Override
    public BatchWorkflow create() {
        return WORKFLOW.get();
    }


    static BatchWorkflow getWorkflow() {
        return WORKFLOW.get();
    }

     static void reset() {
         WORKFLOW.remove();
     }
}
