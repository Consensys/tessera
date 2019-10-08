package com.quorum.tessera.sync;

import java.util.Queue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionPushAckListener implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionPushAckListener.class);

    private final Queue responseQueue;

    private final String correlationId;
        
    public TransactionPushAckListener(Queue responseQueue, String correlationId) {
        this.responseQueue = responseQueue;
        this.correlationId = correlationId;
    }

    @Override
    public void run() {

        while (!responseQueue.contains(correlationId)) {
            ExecutorCallback.execute(
                    () -> {
                        LOGGER.debug(
                                "Response for {} not found yet.",
                                correlationId);
                        TimeUnit.MILLISECONDS.sleep(200);
                        return null;
                    });
        }
        LOGGER.debug("Found response {}", correlationId);
    }

}
