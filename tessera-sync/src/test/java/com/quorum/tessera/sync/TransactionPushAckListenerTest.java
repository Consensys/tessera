package com.quorum.tessera.sync;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import org.junit.Test;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class TransactionPushAckListenerTest {

    @Test
    public void runNoResponse() throws Exception {

        Queue<String> responseQueue = mock(Queue.class);

        String correlationId = "SOME_ID";

        when(responseQueue.contains(correlationId)).thenReturn(false);

        TransactionPushAckListener transactionPushAckListener =
                new TransactionPushAckListener(responseQueue, correlationId);

        CompletableFuture c = CompletableFuture.runAsync(transactionPushAckListener);

        try {
            c.get(50, TimeUnit.MILLISECONDS);
            failBecauseExceptionWasNotThrown(TimeoutException.class);
        } catch (TimeoutException ex) {
            verify(responseQueue, atLeastOnce()).contains(correlationId);
            verifyNoMoreInteractions(responseQueue);
        }
    }

    @Test
    public void runWithResponse() throws Exception {

        Queue<String> responseQueue = mock(Queue.class);

        String correlationId = "SOME_ID";

        when(responseQueue.contains(correlationId)).thenReturn(true);

        TransactionPushAckListener transactionPushAckListener =
                new TransactionPushAckListener(responseQueue, correlationId);

        CompletableFuture c = CompletableFuture.runAsync(transactionPushAckListener);
        c.get(50, TimeUnit.MILLISECONDS);
        verify(responseQueue).contains(correlationId);
        verifyNoMoreInteractions(responseQueue);
    }
}
