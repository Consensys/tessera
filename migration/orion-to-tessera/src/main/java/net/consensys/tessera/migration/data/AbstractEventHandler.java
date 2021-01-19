package net.consensys.tessera.migration.data;

import com.lmax.disruptor.EventHandler;

public abstract class AbstractEventHandler implements EventHandler<OrionRecordEvent> {

    @Override
    public final void onEvent(OrionRecordEvent event, long sequence, boolean endOfBatch) throws Exception {
        if (!event.isError()) {
            this.onEvent(event);
        }
    }

    public abstract void onEvent(OrionRecordEvent event) throws Exception;
}
