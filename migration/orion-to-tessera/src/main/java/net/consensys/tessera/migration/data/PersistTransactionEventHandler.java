package net.consensys.tessera.migration.data;

import com.lmax.disruptor.EventHandler;
import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import javax.json.JsonObject;
import javax.persistence.EntityManagerFactory;
import java.util.Base64;
import java.util.Optional;


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

            EncodedPayload encodedPayload = EncodedPayload.Builder.create()
                .withPrivacyGroupId(privacyGroupId)
                .withSenderKey(senderKey)
                .withCipherTextNonce(new Nonce(new byte[24]))
                .withCipherText(cipherText)
                .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                .withRecipientNonce(nonce)

            .build();

//        PublicKey privacyGroupId = PublicKey.from()
//
//        EncodedPayload encodedPayload =
//            EncodedPayload.Builder.create()
//                .withRecipientKeys(List.copyOf(recipientKeyToBoxes.keySet()))
//                .withRecipientBoxes(
//                    recipientKeyToBoxes.values().stream()
//                        .map(EncryptedKey::getEncoded)
//                        .collect(Collectors.toList()))
//                .withSenderKey(sender)
//                .withPrivacyGroupId(PublicKey.from(orionPrivacyGroupId))
//                .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
//                .withRecipientNonce(recipientNonce)
//                .withCipherTextNonce(new Nonce(new byte[24]))
//                .withCipherText(ciperText)
//                .build();

        MessageHash messageHash =
            Optional.of(event)
                .map(OrionEvent::getKey)
                .map(Base64.getDecoder()::decode)
                .map(MessageHash::new)
                .get();

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
        encryptedTransaction.setHash(messageHash);





    }
}
