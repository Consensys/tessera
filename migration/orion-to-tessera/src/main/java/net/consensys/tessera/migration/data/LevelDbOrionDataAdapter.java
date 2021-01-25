package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmax.disruptor.dsl.Disruptor;
import net.consensys.orion.enclave.PrivacyGroupPayload;
import net.consensys.orion.http.handler.privacy.PrivacyGroup;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;

import javax.json.*;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LevelDbOrionDataAdapter implements OrionDataAdapter {

    private DB leveldb;

    private ObjectMapper cborObjectMapper;

    private Disruptor<OrionEvent> disruptor;

    private volatile Long totalRecords;

    public LevelDbOrionDataAdapter(DB leveldb, ObjectMapper cborObjectMapper, Disruptor<OrionEvent> disruptor) {
        this.leveldb = leveldb;
        this.cborObjectMapper = cborObjectMapper;
        this.disruptor = disruptor;
    }

    @Override
    public void start() throws Exception {

        if (totalRecords == null) {
            DBIterator iterator = leveldb.iterator();
            AtomicLong counter = new AtomicLong(0);
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                counter.incrementAndGet();
            }
            this.totalRecords = counter.get();
        }

        DBIterator iterator = leveldb.iterator();
        AtomicLong counter = new AtomicLong(0);
        for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
            Map.Entry<byte[], byte[]> entry = iterator.peekNext();

            JsonObject jsonObject = cborObjectMapper.readValue(entry.getValue(), JsonObject.class);

            PayloadType payloadType = PayloadType.get(jsonObject);

            if(payloadType == PayloadType.ENCRYPTED_PAYLOAD) {
                JsonUtil.prettyPrint(jsonObject, System.out);
                byte[] privacyGroupId = Optional.of(jsonObject)
                    .map(j -> j.getString("privacyGroupId"))
                    .map(String::getBytes)
                    .get();

                byte[] privacyGroupData = leveldb.get(privacyGroupId);

                if(privacyGroupData != null) {
                    JsonObject privacyGroup = cborObjectMapper.readValue(privacyGroupData, JsonObject.class);
                    List<String> recipientBoxes = privacyGroup.getJsonArray("addresses").stream()
                        .map(JsonString.class::cast)
                        .map(JsonString::getString)
                        .collect(Collectors.toList());

                    jsonObject = Json.createObjectBuilder(jsonObject)
                        .add("recipientBoxes",Json.createArrayBuilder(recipientBoxes)).build();

                }
            }



            System.out.println("Publish " + payloadType);
            JsonUtil.prettyPrint(jsonObject, System.out);
            disruptor.publishEvent(new OrionEvent(payloadType, jsonObject, entry.getKey(), entry.getValue(), totalRecords, counter.incrementAndGet()));

        }

    }
}
