package net.consensys.tessera.migration.data;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslator;

public class TesseraDataEvent<T> implements EventTranslator<TesseraDataEvent> {

  private T entity;

  public static final EventFactory<TesseraDataEvent> FACTORY =
      new EventFactory<TesseraDataEvent>() {
        @Override
        public TesseraDataEvent newInstance() {
          return new TesseraDataEvent();
        }
      };

  private TesseraDataEvent() {}

  public TesseraDataEvent(T entity) {
    this.entity = entity;
  }

  public T getEntity() {
    return entity;
  }

  @Override
  public void translateTo(TesseraDataEvent event, long sequence) {
    event.entity = entity;
  }

  public void reset() {
    this.entity = null;
  }
}
