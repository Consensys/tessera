package com.quorum.tessera.recover.resend;


import static org.mockito.Mockito.mock;

public class MockBatchWorkflowFactory implements BatchWorkflowFactory {

    @Override
    public BatchWorkflow create() {
        return mock(BatchWorkflow.class);
    }
}
