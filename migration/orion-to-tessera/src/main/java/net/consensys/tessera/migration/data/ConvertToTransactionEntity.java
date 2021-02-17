package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
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
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class ConvertToTransactionEntity implements EventHandler<OrionDataEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertToTransactionEntity.class);

    private ObjectMapper cborObjectMapper = JacksonObjectMapperFactory.create();

    private Disruptor<TesseraDataEvent> tesseraDataEventDisruptor;

    public ConvertToTransactionEntity(Disruptor<TesseraDataEvent> tesseraDataEventDisruptor) {
        this.tesseraDataEventDisruptor = tesseraDataEventDisruptor;
    }

    @Override
    public void onEvent(OrionDataEvent event,long sequence,boolean endOfBatch) throws Exception {
        if(event.getPayloadType() != PayloadType.ENCRYPTED_PAYLOAD) {
            LOGGER.debug("Ignoring event {}",event);
            return;
        }

        JsonObject jsonObject = cborObjectMapper.readValue(event.getPayloadData(),JsonObject.class);

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
                .map(OrionDataEvent::getKey)
                .map(MessageHash::new)
                .get();

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
        encryptedTransaction.setHash(messageHash);

        PayloadEncoder payloadEncoder = PayloadEncoder.create();
        byte[] enccodedPayloadData = payloadEncoder.encode(encodedPayload);
        encryptedTransaction.setEncodedPayload(enccodedPayloadData);

        tesseraDataEventDisruptor.publishEvent(new TesseraDataEvent<>(encryptedTransaction));

        LOGGER.info("Published {}", encryptedTransaction);
    }
}
