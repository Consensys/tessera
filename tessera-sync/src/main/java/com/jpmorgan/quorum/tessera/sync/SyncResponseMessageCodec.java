package com.jpmorgan.quorum.tessera.sync;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.websocket.DecodeException;
import javax.websocket.EncodeException;

public class SyncResponseMessageCodec extends CodecAdapter<SyncResponseMessage>  {


    @Override
    public SyncResponseMessage decode(Reader reader) throws DecodeException, IOException {

        try (JsonReader jsonreader = Json.createReader(reader)) {

            final JsonObject jsonObject = jsonreader.readObject();

            final PartyInfo partyInfo = MessageUtil.decodePartyInfoFromBase64(jsonObject.getString("partyInfo"));

            final EncodedPayload transactions = MessageUtil.decodeTransactionsFromBase64(jsonObject.getString("transactions"));

            final long transactionCount = jsonObject.getJsonNumber("transactionCount").longValueExact();
            
            final long transactionOffset = jsonObject.getJsonNumber("transactionOffset").longValueExact();
            
            return SyncResponseMessage.Builder.create()
                    .withPartyInfo(partyInfo)
                    .withTransactions(transactions)
                    .withTransactionCount(transactionCount)
                    .withTransactionOffset(transactionOffset)
                    .build();
        }
    }

    @Override
    public void encode(SyncResponseMessage syncResponseMessage, Writer writer) throws EncodeException, IOException {

        String partyInfo = MessageUtil.encodeToBase64(syncResponseMessage.getPartyInfo());
        String transactions = MessageUtil.encodeToBase64(syncResponseMessage.getTransactions());
        
        JsonObject jsonObject = Json.createObjectBuilder()
                .add("partyInfo", partyInfo)
                .add("transactionCount", syncResponseMessage.getTransactionCount())
                .add("transactionOffset", syncResponseMessage.getTransactionOffset())
                .add("transactions", transactions)
                .build();

        try (JsonWriter jsonwriter = Json.createWriter(writer)) {
            jsonwriter.writeObject(jsonObject);
        }
    }

}
