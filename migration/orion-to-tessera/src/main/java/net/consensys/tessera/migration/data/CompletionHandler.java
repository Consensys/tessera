package net.consensys.tessera.migration.data;

import com.lmax.disruptor.EventHandler;

import java.util.concurrent.CountDownLatch;

public class CompletionHandler implements EventHandler<OrionRecordEvent> {

    private CountDownLatch countDownLatch;

    public CompletionHandler(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void onEvent(OrionRecordEvent event, long sequence, boolean endOfBatch) throws Exception {
        event.reset();
        countDownLatch.countDown();
    }
}
