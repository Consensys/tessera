package net.consensys.tessera.migration.data;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class PersistTransactionEventHandler implements OrionEventHandler {

    private final EntityManagerFactory entityManagerFactory;

    public PersistTransactionEventHandler(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void onEvent(OrionEvent event) throws Exception {
        if(event.getPayloadType() != PayloadType.ENCRYPTED_PAYLOAD) {
            return;
        }

        System.out.println("Save event");
        JsonUtil.prettyPrint(event.getJsonObject(),System.out);

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

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.persist(encryptedTransaction);




    }
}
