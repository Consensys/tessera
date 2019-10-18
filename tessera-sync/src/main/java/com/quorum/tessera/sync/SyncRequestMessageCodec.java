package com.quorum.tessera.sync;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Objects;
import java.util.Optional;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.websocket.DecodeException;
import javax.websocket.EncodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncRequestMessageCodec extends TextCodecAdapter<SyncRequestMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncRequestMessageCodec.class);

    @Override
    public SyncRequestMessage decode(Reader reader) throws DecodeException, IOException {

        try (JsonReader jsonReader = Json.createReader(reader)) {
            final JsonObject json = jsonReader.readObject();

            SyncRequestMessage.Type type = SyncRequestMessage.Type.valueOf(json.getString("type"));
            SyncRequestMessage.Builder messageBuilder = SyncRequestMessage.Builder.create(type);

            if (json.containsKey("correlationId")) {
                String correlationId = json.getString("correlationId");
                messageBuilder.withCorrelationId(correlationId);
            }

            if (type == SyncRequestMessage.Type.PARTY_INFO) {

                if (json.containsKey("partyInfo")) {
                    final String partyInfoData = json.getString("partyInfo");
                    final PartyInfo partyInfo = MessageUtil.decodePartyInfoFromBase64(partyInfoData);
                    messageBuilder.withPartyInfo(partyInfo);
                }
            }

            if (type == SyncRequestMessage.Type.TRANSACTION_PUSH) {
                final String transactionData = json.getString("transactions");

                if (json.containsKey("recipientKey")) {
                    final String recipientKey = json.getString("recipientKey");
                    messageBuilder.withRecipientKey(MessageUtil.decodePublicKeyFromBase64(recipientKey));
                }
                final EncodedPayload transaction = MessageUtil.decodeTransactionsFromBase64(transactionData);
                messageBuilder.withTransactions(transaction);
            }

            if (type == SyncRequestMessage.Type.TRANSACTION_SYNC) {
                if (json.containsKey("recipientKey")) {
                    final String recipientKey = json.getString("recipientKey");
                    messageBuilder.withRecipientKey(MessageUtil.decodePublicKeyFromBase64(recipientKey));
                }
            }

            return messageBuilder.build();
        }
    }

    @Override
    public void encode(SyncRequestMessage syncRequestMessage, Writer writer) throws EncodeException, IOException {
        LOGGER.debug("Encode : {}", syncRequestMessage);
        JsonObjectBuilder jsonObjectBuilder =
                Json.createObjectBuilder().add("type", syncRequestMessage.getType().name());

        if (Objects.nonNull(syncRequestMessage.getCorrelationId())) {
            jsonObjectBuilder.add("correlationId", syncRequestMessage.getCorrelationId());
        }
        if (syncRequestMessage.getType() == SyncRequestMessage.Type.PARTY_INFO) {

            Optional.ofNullable(syncRequestMessage.getPartyInfo())
                    .map(MessageUtil::encodeToBase64)
                    .ifPresent(
                            p -> {
                                jsonObjectBuilder.add("partyInfo", p);
                            });

        } else {

            if (syncRequestMessage.getType() == SyncRequestMessage.Type.TRANSACTION_PUSH) {
                jsonObjectBuilder.add("transactions", MessageUtil.encodeToBase64(syncRequestMessage.getTransactions()));
            }

            if (syncRequestMessage.getRecipientKey() != null) {
                jsonObjectBuilder.add("recipientKey", MessageUtil.encodeToBase64(syncRequestMessage.getRecipientKey()));
            }
        }

        Json.createWriter(writer).writeObject(jsonObjectBuilder.build());
    }
}
