package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmax.disruptor.dsl.Disruptor;
import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.encryption.PublicKey;
import net.consensys.orion.enclave.EncryptedPayload;
import net.consensys.orion.enclave.PrivacyGroupPayload;
import net.consensys.tessera.migration.OrionKeyHelper;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;

import javax.json.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class LevelDbOrionDataAdapter implements OrionDataAdapter {

    private final DB leveldb;

    private final ObjectMapper cborObjectMapper;

    private final Disruptor<OrionEvent> disruptor;

    private final OrionKeyHelper orionKeyHelper;

    private volatile Long totalRecords;

    public LevelDbOrionDataAdapter(DB leveldb, ObjectMapper cborObjectMapper, Disruptor<OrionEvent> disruptor,OrionKeyHelper orionKeyHelper) {
        this.leveldb = Objects.requireNonNull(leveldb);
        this.cborObjectMapper = Objects.requireNonNull(cborObjectMapper);
        this.disruptor = Objects.requireNonNull(disruptor);
        this.orionKeyHelper = Objects.requireNonNull(orionKeyHelper);
    }

    @Override
    public void start() throws Exception {

        if (Objects.isNull(totalRecords)) {

            DBIterator iterator = leveldb.iterator();
            AtomicLong counter = new AtomicLong(0);
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                counter.incrementAndGet();
            }
            totalRecords = counter.get();
        }

        DBIterator iterator = leveldb.iterator();
        for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
            Map.Entry<byte[], byte[]> entry = iterator.peekNext();

            final JsonObject jsonObject = cborObjectMapper.readValue(entry.getValue(), JsonObject.class);

            final PayloadType payloadType = PayloadType.get(jsonObject);

            final OrionEvent.Builder orionEvent = OrionEvent.Builder.create()
                .withTotalEventCount(totalRecords)
                .withJsonObject(jsonObject)
                .withKey(entry.getKey())
                .withPayloadType(payloadType);

            if(payloadType == PayloadType.ENCRYPTED_PAYLOAD) {

                EncryptedPayload encryptedPayload = cborObjectMapper.readValue(entry.getValue(),EncryptedPayload.class);

                byte[] privacyGroupId = encryptedPayload.privacyGroupId();
                byte[] privacyGroupData = leveldb.get(privacyGroupId);

                if(privacyGroupData != null) {
                    PrivacyGroupPayload privacyGroup = cborObjectMapper.readValue(privacyGroupData, PrivacyGroupPayload.class);
                    Map<PublicKey, RecipientBox> recipientBoxMap = new RecipientBoxHelper(orionKeyHelper,encryptedPayload,privacyGroup).getRecipientMapping();
                    orionEvent.withRecipientBoxMap(recipientBoxMap);
                }
            }

            disruptor.publishEvent(orionEvent.build());


        }

    }
}
