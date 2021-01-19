package net.consensys.tessera.migration.data;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import net.consensys.orion.enclave.EncryptedKey;
import net.consensys.orion.enclave.EncryptedPayload;
import org.apache.tuweni.crypto.sodium.Box;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import java.util.*;
import java.util.stream.Collectors;

public class PersistEventHandler extends AbstractEventHandler {

    private EntityManagerFactory entityManagerFactory;

    public PersistEventHandler(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void onEvent(OrionRecordEvent event) throws Exception {

        Map<PublicKey, EncryptedKey> recipientKeyToBoxes =
                event.getRecipientKeyToBoxes().entrySet().stream()
                        .sorted(Map.Entry.<String, EncryptedKey>comparingByKey())
                        .collect(
                                Collectors.toMap(
                                        e ->
                                                Optional.of(e.getKey())
                                                        .map(Base64.getDecoder()::decode)
                                                        .map(PublicKey::from)
                                                        .get(),
                                        e ->
                                                Optional.of(e.getValue())
                                                        //  .map(Base64.getDecoder()::decode)
                                                        .get(),
                                        (l, r) -> l,
                                        LinkedHashMap::new));

        PublicKey sender =
                Optional.of(event)
                        .map(OrionRecordEvent::getEncryptedPayload)
                        .map(EncryptedPayload::sender)
                        .map(Box.PublicKey::bytesArray)
                        .map(PublicKey::from)
                        .get();

        Nonce recipientNonce =
                Optional.of(event)
                        .map(OrionRecordEvent::getEncryptedPayload)
                        .map(EncryptedPayload::nonce)
                        .map(Nonce::new)
                        .get();

        byte[] ciperText =
                Optional.of(event).map(OrionRecordEvent::getEncryptedPayload).map(EncryptedPayload::cipherText).get();

        EncodedPayload encodedPayload =
                EncodedPayload.Builder.create()
                        .withRecipientKeys(List.copyOf(recipientKeyToBoxes.keySet()))
                        .withRecipientBoxes(
                                recipientKeyToBoxes.values().stream()
                                        .map(EncryptedKey::getEncoded)
                                        .collect(Collectors.toList()))
                        .withSenderKey(sender)
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withRecipientNonce(recipientNonce)
                        .withCipherTextNonce(new Nonce(new byte[24]))
                        .withCipherText(ciperText)
                        .build();

        byte[] encodedPayloadData = PayloadEncoder.create().encode(encodedPayload);

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
        encryptedTransaction.setEncodedPayload(encodedPayloadData);

        MessageHash messageHash =
                Optional.of(event)
                        .map(OrionRecordEvent::getKey)
                        .map(Base64.getDecoder()::decode)
                        .map(MessageHash::new)
                        .get();

        encryptedTransaction.setHash(messageHash);

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();
        try {
            entityManager.persist(encryptedTransaction);
            entityManager.getTransaction().commit();
        } catch (PersistenceException ex) {
            ex.printStackTrace();
            entityManager.getTransaction().rollback();
        }

        System.out.println("Save " + encryptedTransaction);
    }
}
