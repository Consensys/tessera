package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmax.disruptor.dsl.Disruptor;
import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.encryption.Encryptor;
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

    private final AtomicLong totalRecords = new AtomicLong(0);

    private final EncryptedKeyMatcher encryptedKeyMatcher;

    public LevelDbOrionDataAdapter(DB leveldb, ObjectMapper cborObjectMapper, Disruptor<OrionEvent> disruptor,OrionKeyHelper orionKeyHelper,Encryptor encryptor) {
        this.leveldb = Objects.requireNonNull(leveldb);
        this.cborObjectMapper = Objects.requireNonNull(cborObjectMapper);
        this.disruptor = Objects.requireNonNull(disruptor);
        this.orionKeyHelper = Objects.requireNonNull(orionKeyHelper);
        this.encryptedKeyMatcher = new EncryptedKeyMatcher(orionKeyHelper,encryptor);
    }

    @Override
    public void start() throws Exception {

        if (totalRecords.get() == 0) {
            DBIterator iterator = leveldb.iterator();
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                totalRecords.incrementAndGet();
            }
        }

        AtomicLong eventCounter = new AtomicLong(0);
        DBIterator iterator = leveldb.iterator();
        for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
            Map.Entry<byte[], byte[]> entry = iterator.peekNext();

            final JsonObject jsonObject = cborObjectMapper.readValue(entry.getValue(), JsonObject.class);

            final PayloadType payloadType = PayloadType.get(jsonObject);

            final OrionEvent.Builder orionEventBuilder = OrionEvent.Builder.create()
                .withTotalEventCount(totalRecords.get())
                .withEventNumber(eventCounter.incrementAndGet())
                .withJsonObject(jsonObject)
                .withKey(entry.getKey())
                .withPayloadType(payloadType);

            if(payloadType == PayloadType.ENCRYPTED_PAYLOAD) {

                EncryptedPayload encryptedPayload = cborObjectMapper.readValue(entry.getValue(),EncryptedPayload.class);

                byte[] privacyGroupId = encryptedPayload.privacyGroupId();

                byte[] privacyGroupKey = Base64.getEncoder().encode(privacyGroupId);
                byte[] privacyGroupData = leveldb.get(privacyGroupKey);

                if(privacyGroupData != null) {

                    PrivacyGroupPayload privacyGroup = cborObjectMapper.readValue(privacyGroupData, PrivacyGroupPayload.class);
                    Map<PublicKey, RecipientBox> recipientBoxMap = new RecipientBoxHelper(orionKeyHelper,encryptedPayload,privacyGroup).getRecipientMapping();
                    orionEventBuilder
                        .withRecipientBoxMap(recipientBoxMap);

                } else {
                    //TODO: Handle
                    assert encryptedPayload.encryptedKeys().length == 1 : "There must only be one encryptedKey";

                    final PublicKey recipientKey = encryptedKeyMatcher.findRecipientKeyWhenNotSenderAndPrivacyGroupNotFound(encryptedPayload).get();
                    final RecipientBox recipientBox = RecipientBox.from(encryptedPayload.encryptedKeys()[0].getEncoded());

                    final Map<PublicKey,RecipientBox> recipientBoxMap = Map.of(recipientKey,recipientBox);
                    orionEventBuilder
                        .withRecipientBoxMap(recipientBoxMap);
                }
            }
            disruptor.publishEvent(orionEventBuilder.build());

        }

    }
}
