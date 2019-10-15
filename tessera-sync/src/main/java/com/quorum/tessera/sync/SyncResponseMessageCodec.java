package com.quorum.tessera.sync;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.time.Instant;
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

public class SyncResponseMessageCodec extends TextCodecAdapter<SyncResponseMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncResponseMessageCodec.class);

    @Override
    public SyncResponseMessage decode(Reader reader) throws DecodeException, IOException {

        try (JsonReader jsonreader = Json.createReader(reader)) {

            final JsonObject jsonObject = jsonreader.readObject();

            final SyncResponseMessage.Type type = SyncResponseMessage.Type.valueOf(jsonObject.getString("type"));

            final SyncResponseMessage.Builder responseBuilder = SyncResponseMessage.Builder.create(type);

            if (type == SyncResponseMessage.Type.PARTY_INFO) {

                final PartyInfo partyInfo =
                        Optional.of(jsonObject)
                                .filter(o -> o.containsKey("partyInfo"))
                                .map(o -> o.getString("partyInfo"))
                                .map(MessageUtil::decodePartyInfoFromBase64)
                                .orElse(null);

                return responseBuilder.withPartyInfo(partyInfo).build();
            }

            final EncodedPayload transactions =
                    MessageUtil.decodeTransactionsFromBase64(jsonObject.getString("transactions"));

            final long transactionCount = jsonObject.getJsonNumber("transactionCount").longValueExact();

            final long transactionOffset = jsonObject.getJsonNumber("transactionOffset").longValueExact();

            return responseBuilder
                    .withTransactions(transactions)
                    .withTransactionCount(transactionCount)
                    .withTransactionOffset(transactionOffset)
                    .build();
        }
    }

    @Override
    public void encode(SyncResponseMessage syncResponseMessage, Writer writer) throws EncodeException, IOException {

        LOGGER.debug("Encode : {}", syncResponseMessage);

        final JsonObjectBuilder jsonObjectBuilder =
                Json.createObjectBuilder().add("type", syncResponseMessage.getType().name());

        if (syncResponseMessage.getType() == SyncResponseMessage.Type.PARTY_INFO) {

            PartyInfo partyInfo = syncResponseMessage.getPartyInfo();
            if (Objects.nonNull(partyInfo)) {
                Instant lastContacted = Instant.now();
                partyInfo.getParties().forEach(p -> p.setLastContacted(lastContacted));
            }

            Optional.ofNullable(partyInfo)
                    .map(MessageUtil::encodeToBase64)
                    .ifPresent(
                            p -> {
                                jsonObjectBuilder.add("partyInfo", p);
                            });

        } else {

            final String transactions = MessageUtil.encodeToBase64(syncResponseMessage.getTransactions());

            jsonObjectBuilder
                    .add("transactionCount", syncResponseMessage.getTransactionCount())
                    .add("transactionOffset", syncResponseMessage.getTransactionOffset())
                    .add("transactions", transactions);
        }

        JsonObject jsonObject = jsonObjectBuilder.build();

        Json.createWriter(writer).writeObject(jsonObject);
    }
}
