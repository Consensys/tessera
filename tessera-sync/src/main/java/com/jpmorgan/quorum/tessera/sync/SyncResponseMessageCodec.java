package com.jpmorgan.quorum.tessera.sync;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.websocket.DecodeException;
import javax.websocket.EncodeException;

public class SyncResponseMessageCodec extends TextStreamCodecAdapter<SyncResponseMessage> {

    @Override
    public SyncResponseMessage decode(Reader reader) throws DecodeException, IOException {

        try (JsonReader jsonreader = Json.createReader(reader)) {

            final JsonObject jsonObject = jsonreader.readObject();

            final SyncResponseMessage.Type type = SyncResponseMessage.Type.valueOf(jsonObject.getString("type"));

            final SyncResponseMessage.Builder responseBuilder = SyncResponseMessage.Builder.create(type);

            if (type == SyncResponseMessage.Type.PARTY_INFO) {
                final PartyInfo partyInfo = MessageUtil.decodePartyInfoFromBase64(jsonObject.getString("partyInfo"));
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

        final JsonObjectBuilder jsonObjectBuilder =
                Json.createObjectBuilder().add("type", syncResponseMessage.getType().name());

        if (syncResponseMessage.getType() == SyncResponseMessage.Type.PARTY_INFO) {
            final String partyInfo = MessageUtil.encodeToBase64(syncResponseMessage.getPartyInfo());
            jsonObjectBuilder.add("partyInfo", partyInfo);
        } else {

            final String transactions = MessageUtil.encodeToBase64(syncResponseMessage.getTransactions());

            jsonObjectBuilder
                    .add("transactionCount", syncResponseMessage.getTransactionCount())
                    .add("transactionOffset", syncResponseMessage.getTransactionOffset())
                    .add("transactions", transactions);
        }

        try (JsonWriter jsonwriter = Json.createWriter(writer)) {
            jsonwriter.writeObject(jsonObjectBuilder.build());
        }
    }
}
