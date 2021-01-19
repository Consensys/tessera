package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmax.disruptor.dsl.Disruptor;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;

import javax.json.JsonObject;
import java.util.Map;

public class LevelDbOrionDataAdapter implements OrionDataAdapter {

    private DB leveldb;

    private ObjectMapper cborObjectMapper;

    private Disruptor<OrionRecordEvent> disruptor;

    public LevelDbOrionDataAdapter(DB leveldb, ObjectMapper cborObjectMapper, Disruptor<OrionRecordEvent> disruptor) {
        this.leveldb = leveldb;
        this.cborObjectMapper = cborObjectMapper;
        this.disruptor = disruptor;
    }

    @Override
    public void start() throws Exception {
        DBIterator iterator = leveldb.iterator();

        for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
            Map.Entry<byte[], byte[]> entry = iterator.peekNext();

            String key = new String(entry.getKey());
            byte[] value = entry.getValue();

            JsonObject jsonObject = cborObjectMapper.readValue(value, JsonObject.class);

            PayloadType payloadType = PayloadType.get(jsonObject);

            if (payloadType == PayloadType.EncryptedPayload) {
                if (jsonObject.containsKey("privacyGroupId")) {
                    System.out.println("Publishing " + key);
                    disruptor.publishEvent(new OrionRecordEvent(InputType.LEVELDB, key, value));
                    System.out.println("Published " + key);
                }
            }
        }
    }
}
