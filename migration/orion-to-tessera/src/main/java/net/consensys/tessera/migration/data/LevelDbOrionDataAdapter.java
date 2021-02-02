package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmax.disruptor.dsl.Disruptor;
import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.encryption.PublicKey;
import net.consensys.orion.enclave.EncryptedPayload;
import net.consensys.orion.enclave.PrivacyGroupPayload;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class LevelDbOrionDataAdapter implements OrionDataAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LevelDbOrionDataAdapter.class);

    private final DB leveldb;

    private final ObjectMapper cborObjectMapper;

    private final Disruptor<OrionEvent> disruptor;

    private final AtomicLong totalRecords = new AtomicLong(0);

    private final EncryptedKeyMatcher encryptedKeyMatcher;

    private final RecipientBoxHelper recipientBoxHelper;

    public LevelDbOrionDataAdapter(DB leveldb, ObjectMapper cborObjectMapper, Disruptor<OrionEvent> disruptor,EncryptedKeyMatcher encryptedKeyMatcher,
                                   RecipientBoxHelper recipientBoxHelper) {
        this.leveldb = Objects.requireNonNull(leveldb);
        this.cborObjectMapper = Objects.requireNonNull(cborObjectMapper);
        this.disruptor = Objects.requireNonNull(disruptor);
        this.encryptedKeyMatcher = Objects.requireNonNull(encryptedKeyMatcher);
        this.recipientBoxHelper = Objects.requireNonNull(recipientBoxHelper);
    }

    @Override
    public void start() throws Exception {

        if (totalRecords.get() == 0) {
            DBIterator iterator = leveldb.iterator();
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                totalRecords.incrementAndGet();
            }
            LOGGER.info("Total records {}",totalRecords.get());
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
                .withKey(Base64.getEncoder().encode(entry.getKey()))
                .withPayloadType(payloadType);

            if(payloadType == PayloadType.ENCRYPTED_PAYLOAD) {

                EncryptedPayload encryptedPayload = cborObjectMapper.readValue(entry.getValue(),EncryptedPayload.class);

                byte[] privacyGroupId = Optional.of(encryptedPayload)
                    .map(EncryptedPayload::privacyGroupId)
                    .map(Base64.getEncoder()::encode)
                    .get();

                byte[] privacyGroupData = leveldb.get(privacyGroupId);

                if(privacyGroupData != null) {

                    PrivacyGroupPayload privacyGroup = cborObjectMapper.readValue(privacyGroupData, PrivacyGroupPayload.class);
                    Map<PublicKey, RecipientBox> recipientBoxMap = recipientBoxHelper.getRecipientMapping(encryptedPayload,privacyGroup);
                    orionEventBuilder
                        .withRecipientBoxMap(recipientBoxMap);

                } else if(encryptedPayload.encryptedKeys().length == 1) {

                    final PublicKey recipientKey = encryptedKeyMatcher.findRecipientKeyWhenNotSenderAndPrivacyGroupNotFound(encryptedPayload).get();
                    final RecipientBox recipientBox = RecipientBox.from(encryptedPayload.encryptedKeys()[0].getEncoded());

                    final Map<PublicKey,RecipientBox> recipientBoxMap = Map.of(recipientKey,recipientBox);
                    orionEventBuilder
                        .withRecipientBoxMap(recipientBoxMap);
                } else {
                    throw new UnsupportedOperationException("");
                }
            }
            disruptor.publishEvent(orionEventBuilder.build());
        }
        LOGGER.info("All records published.");
    }
}
