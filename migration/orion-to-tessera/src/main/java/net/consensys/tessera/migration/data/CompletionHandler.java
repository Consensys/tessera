package net.consensys.tessera.migration.data;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class CompletionHandler implements OrionEventHandler {

    private final AtomicLong counter = new AtomicLong(0);

    private final CountDownLatch countDownLatch;

    public CompletionHandler() {
        this.countDownLatch = new CountDownLatch(1);
    }

    public void await() throws InterruptedException {
        countDownLatch.await();
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
