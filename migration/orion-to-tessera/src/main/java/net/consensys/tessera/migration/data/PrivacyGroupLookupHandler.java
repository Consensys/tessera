package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmax.disruptor.EventHandler;
import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.encryption.PublicKey;
import net.consensys.orion.enclave.EncryptedKey;
import net.consensys.orion.enclave.EncryptedPayload;

import java.util.*;

public class PrivacyGroupLookupHandler implements EventHandler<OrionDataEvent> {

    private ObjectMapper cborObjectMapper = JacksonObjectMapperFactory.create();

    private JsonFactory jacksonJsonFactory = JacksonObjectMapperFactory.createFactory();

    private RecipientBoxHelper recipientBoxHelper;

    private EncryptedKeyMatcher encryptedKeyMatcher;

    public PrivacyGroupLookupHandler(RecipientBoxHelper recipientBoxHelper, EncryptedKeyMatcher encryptedKeyMatcher) {
        this.recipientBoxHelper = recipientBoxHelper;
        this.encryptedKeyMatcher = encryptedKeyMatcher;
    }

    @Override
    public void onEvent(OrionDataEvent orionDataEvent, long sequence, boolean endOfBatch) throws Exception {
        if(orionDataEvent.getPayloadType() != PayloadType.ENCRYPTED_PAYLOAD) {
            return;
        }

        EncryptedPayload orionEncryptedPayload = cborObjectMapper.readValue(orionDataEvent.getPayloadData(),EncryptedPayload.class);

        byte[] privacyGroupData = orionDataEvent.getPrivacyGroupData();

        if(Objects.nonNull(privacyGroupData)) {

            try(JsonParser jParser = jacksonJsonFactory.createParser(privacyGroupData)) {

                List<String> recipients = new ArrayList<>();
                while (!jParser.isClosed()) {

                    JsonToken jsonToken = jParser.nextToken();
                    if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                        String fieldname = jParser.getCurrentName();
                        if (Objects.equals(fieldname, "addresses")) {
                            jParser.nextToken();
                            while (jParser.nextToken() != JsonToken.END_ARRAY) {
                                String address = jParser.getValueAsString();
                                recipients.add(address);
                            }
                        }
                    }
                }

                Map<PublicKey, RecipientBox> recipientBoxMap = recipientBoxHelper.getRecipientMapping(orionEncryptedPayload,recipients);
                orionDataEvent.setRecipientBoxMap(recipientBoxMap);

            }
        } else if(orionEncryptedPayload.encryptedKeys().length == 1) {
            final PublicKey recipientKey = encryptedKeyMatcher.findRecipientKeyWhenNotSenderAndPrivacyGroupNotFound(orionEncryptedPayload).get();

            final RecipientBox recipientBox = Optional.of(orionEncryptedPayload.encryptedKeys()[0])
                .map(EncryptedKey::getEncoded)
                .map(RecipientBox::from)
                .get();

            orionDataEvent.setRecipientBoxMap(Map.of(recipientKey,recipientBox));

        } else {
            throw new UnsupportedOperationException("");
        }
    }
}
