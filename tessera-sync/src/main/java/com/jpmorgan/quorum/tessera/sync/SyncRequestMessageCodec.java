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

public class SyncRequestMessageCodec extends TextStreamCodecAdapter<SyncRequestMessage> {

    @Override
    public SyncRequestMessage decode(Reader reader) throws DecodeException, IOException {

        try (JsonReader jsonReader = Json.createReader(reader)) {
            final JsonObject json = jsonReader.readObject();

            SyncRequestMessage.Type type = SyncRequestMessage.Type.valueOf(json.getString("type"));
            SyncRequestMessage.Builder messageBuilder = SyncRequestMessage.Builder.create(type);

            if (type == SyncRequestMessage.Type.PARTY_INFO) {
                final String partyInfoData = json.getString("partyInfo");
                final PartyInfo partyInfo = MessageUtil.decodePartyInfoFromBase64(partyInfoData);

                messageBuilder.withPartyInfo(partyInfo);
            }

            if (type == SyncRequestMessage.Type.TRANSACTION_PUSH) {
                final String transactionData = json.getString("transactions");
                final EncodedPayload transaction = MessageUtil.decodeTransactionsFromBase64(transactionData);
                messageBuilder.withTransactions(transaction);
            }

            return messageBuilder.build();
        }
    }

    @Override
    public void encode(SyncRequestMessage syncRequestMessage, Writer writer) throws EncodeException, IOException {

        JsonObjectBuilder jsonObjectBuilder =
                Json.createObjectBuilder().add("type", syncRequestMessage.getType().name());

        final JsonObject json;
        if (syncRequestMessage.getType() == SyncRequestMessage.Type.PARTY_INFO) {
            jsonObjectBuilder.add("partyInfo", MessageUtil.encodeToBase64(syncRequestMessage.getPartyInfo()));
        } else {
            jsonObjectBuilder.add("transactions", MessageUtil.encodeToBase64(syncRequestMessage.getTransactions()));
        }

        try (JsonWriter jsonWriter = Json.createWriter(writer)) {
            jsonWriter.writeObject(jsonObjectBuilder.build());
        }
    }
}
