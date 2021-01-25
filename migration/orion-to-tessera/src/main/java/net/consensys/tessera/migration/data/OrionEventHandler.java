package net.consensys.tessera.migration.data;

import com.lmax.disruptor.EventHandler;

public interface OrionEventHandler extends EventHandler<OrionEvent> {

    @Override
    default void onEvent(OrionEvent orionEvent, long sequence, boolean endOfBatch) throws Exception {
        this.onEvent(orionEvent);
    }

    void onEvent(OrionEvent orionEvent) throws Exception;


}
