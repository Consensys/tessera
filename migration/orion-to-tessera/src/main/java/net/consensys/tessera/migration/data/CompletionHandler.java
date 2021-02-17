package net.consensys.tessera.migration.data;


import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class CompletionHandler implements EventHandler<OrionDataEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompletionHandler.class);

    private final AtomicLong counter = new AtomicLong(0);

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    @Override
    public void onEvent(OrionDataEvent event,long sequence,boolean endOfBatch) throws Exception {
        long count = counter.incrementAndGet();
        LOGGER.info("Completed event {}",event);
        if(count == event.getTotalEventCount()) {
            countDownLatch.countDown();
        }
        event.reset();
    }

    public void await() throws InterruptedException {
        countDownLatch.await();
    }
}
