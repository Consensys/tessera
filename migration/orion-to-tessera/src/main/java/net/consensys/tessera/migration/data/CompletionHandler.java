package net.consensys.tessera.migration.data;

import com.lmax.disruptor.EventHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class CompletionHandler implements EventHandler<OrionEvent> {

    private AtomicLong counter = new AtomicLong(0);

    private CountDownLatch countDownLatch;

    public CompletionHandler(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void onEvent(OrionEvent event, long sequence, boolean endOfBatch) throws Exception {
        long count = counter.incrementAndGet();
        if(count == event.getTotalEventCount()) {
            countDownLatch.countDown();
        }
        event.reset();
    }
}
