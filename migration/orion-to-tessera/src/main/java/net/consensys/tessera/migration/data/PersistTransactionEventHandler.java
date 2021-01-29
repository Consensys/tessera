package net.consensys.tessera.migration.data;

import com.lmax.disruptor.EventHandler;
import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.*;
import java.util.stream.Collectors;


public class PersistTransactionEventHandler implements EventHandler<OrionEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistTransactionEventHandler.class);

    private final EntityManagerFactory entityManagerFactory;

    private EntityManager entityManager;

    private ThreadLocal<EntityTransaction> txn = new ThreadLocal<>();

    public PersistTransactionEventHandler(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.entityManager = entityManagerFactory.createEntityManager();
    }

    @Override
    public void onEvent(OrionEvent event,long sequence,boolean endOfBatch) throws Exception {
        if(event.getPayloadType() != PayloadType.ENCRYPTED_PAYLOAD) {
            LOGGER.debug("Ignoring event {}",event);
            return;
        }

        JsonObject jsonObject = event.getJsonObject();

        PublicKey privacyGroupId = Optional.of(jsonObject)
            .map(j -> j.getString("privacyGroupId"))
            .map(Base64.getDecoder()::decode)
            .map(PublicKey::from).get();

        PublicKey senderKey = Optional.of(jsonObject)
            .map(j -> j.getString("sender"))
            .map(Base64.getDecoder()::decode)
            .map(PublicKey::from).get();

        Nonce nonce = Optional.of(jsonObject).map(j -> j.getString("nonce"))
            .map(Base64.getDecoder()::decode).map(Nonce::new).get();

        byte[] cipherText = Optional.of(jsonObject)
            .map(j -> j.getString("cipherText"))
            .map(Base64.getDecoder()::decode)
            .get();


        Map<PublicKey, RecipientBox> recipientKeyToBoxes = event.getRecipientBoxMap().orElse(Map.of());

            EncodedPayload encodedPayload = EncodedPayload.Builder.create()
                .withPrivacyGroupId(privacyGroupId)
                .withRecipientKeys(List.copyOf(recipientKeyToBoxes.keySet()))
                .withRecipientBoxes(recipientKeyToBoxes.values()
                    .stream()
                    .map(RecipientBox::getData)
                    .collect(Collectors.toList()))
                .withSenderKey(senderKey)
                .withCipherTextNonce(new Nonce(new byte[24]))
                .withCipherText(cipherText)
                .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                .withRecipientNonce(nonce)
            .build();

        MessageHash messageHash =
            Optional.of(event)
                .map(OrionEvent::getKey)
                .map(Base64.getDecoder()::decode)
                .map(MessageHash::new)
                .get();

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
        encryptedTransaction.setHash(messageHash);

        PayloadEncoder payloadEncoder = PayloadEncoder.create();
        byte[] enccodedPayloadData = payloadEncoder.encode(encodedPayload);
        encryptedTransaction.setEncodedPayload(enccodedPayloadData);

        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();
        entityManager.persist(encryptedTransaction);
        entityTransaction.commit();
        LOGGER.info("Persisted {}", event);
    }
}
