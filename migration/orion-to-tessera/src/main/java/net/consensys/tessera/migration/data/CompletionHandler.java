package net.consensys.tessera.migration.data;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class CompletionHandler implements OrionEventHandler {

    private AtomicLong counter = new AtomicLong(0);

    private CountDownLatch countDownLatch;

    public CompletionHandler(CountDownLatch countDownLatch) {
        this.countDownLatch = Objects.requireNonNull(countDownLatch);
    }

    @Override
    public void onEvent(OrionEvent event) throws Exception {
        long count = counter.incrementAndGet();
        if(count == event.getTotalEventCount()) {
            countDownLatch.countDown();
        }
        event.reset();
    }
}
