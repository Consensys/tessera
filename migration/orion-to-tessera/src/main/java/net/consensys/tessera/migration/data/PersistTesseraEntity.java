package net.consensys.tessera.migration.data;

import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;

public class PersistTesseraEntity implements EventHandler<TesseraDataEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistTesseraEntity.class);

    private EntityManager entityManager;

    public PersistTesseraEntity(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void onEvent(TesseraDataEvent event, long sequence, boolean endOfBatch) throws Exception {
        LOGGER.info("Persist {}",event);
        entityManager.persist(event.getEntity());
    }
}
