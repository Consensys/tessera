package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.lmax.disruptor.dsl.Disruptor;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class LevelDbDataProducer implements DataProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(LevelDbDataProducer.class);

    private final DB leveldb;

    private final Disruptor<OrionDataEvent> disruptor;


    public LevelDbDataProducer(DB leveldb, Disruptor<OrionDataEvent> disruptor) {
        this.leveldb = Objects.requireNonNull(leveldb);
        this.disruptor = Objects.requireNonNull(disruptor);
    }

    public void start() throws Exception {

        AtomicLong eventCounter = new AtomicLong(0);
        DBIterator iterator = leveldb.iterator();
        for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
            Map.Entry<byte[], byte[]> entry = iterator.peekNext();

            byte[] key = entry.getKey();
            byte[] value = entry.getValue();

            PayloadType payloadType = PayloadType.parsePayloadType(value);

            final OrionDataEvent.Builder orionDataEventBuilder
                = OrionDataEvent.Builder.create()
                .withEventNumber(eventCounter.incrementAndGet())
                .withTotalEventCount((long) MigrationInfo.getInstance().getRowCount())
                .withPayloadData(value)
                .withPayloadType(payloadType)
                .withKey(key);

           if(payloadType == PayloadType.ENCRYPTED_PAYLOAD) {
                byte[] privacyGroupId = findPrivacyGroupId(value).get();
                byte[] privacyGroupData = leveldb.get(privacyGroupId);
                if(Objects.nonNull(privacyGroupData)) {
                    orionDataEventBuilder.withPrivacyGroupData(privacyGroupData);
                }
           }

            final OrionDataEvent orionDataEvent = orionDataEventBuilder.build();
            disruptor.publishEvent(orionDataEvent);

        }
        LOGGER.info("All {} records published.",MigrationInfo.getInstance().getRowCount());
    }

    private Optional<byte[]> findPrivacyGroupId(byte[] data) throws IOException {

        final JsonFactory jacksonJsonFactory = JacksonObjectMapperFactory.createFactory();

        try(JsonParser jacksonJsonParser = jacksonJsonFactory.createParser(data)) {

            while (!jacksonJsonParser.isClosed()) {

                JsonToken jsonToken = jacksonJsonParser.nextToken();
                if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                    String fieldname = jacksonJsonParser.getCurrentName();
                    if(Objects.equals("privacyGroupId",fieldname)) {
                        jacksonJsonParser.nextToken();
                        byte[] d = Base64.getEncoder().encode(jacksonJsonParser.getBinaryValue());
                        return Optional.of(d);
                    }
                }
            }

        }
        return Optional.empty();
    }

}
