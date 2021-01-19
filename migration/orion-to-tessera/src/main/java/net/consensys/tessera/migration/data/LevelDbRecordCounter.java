package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;

import javax.json.JsonObject;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class LevelDbRecordCounter implements RecordCounter {

    private DB leveldb;

    private ObjectMapper cborObjectMapper;

    public LevelDbRecordCounter(DB leveldb, ObjectMapper cborObjectMapper) {
        this.leveldb = leveldb;
        this.cborObjectMapper = cborObjectMapper;
    }

    @Override
    public long count() throws Exception {
        AtomicLong counter = new AtomicLong(0);
        DBIterator iterator = leveldb.iterator();

        for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
            Map.Entry<byte[], byte[]> entry = iterator.peekNext();
            byte[] value = entry.getValue();
            JsonObject jsonObject = cborObjectMapper.readValue(value, JsonObject.class);
            PayloadType payloadType = PayloadType.get(jsonObject);
            if (payloadType == PayloadType.EncryptedPayload) {

                if (jsonObject.containsKey("privacyGroupId")) {
                    String privacyGroupId = jsonObject.getString("privacyGroupId");
                    //  if(leveldb.get(privacyGroupId.getBytes(StandardCharsets.UTF_8)) != null) {
                    counter.incrementAndGet();
                    // }
                }
            }
        }

        return counter.get();
    }
}
