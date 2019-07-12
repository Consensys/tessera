package com.jpmorgan.quorum.tessera.sync;

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

public class SyncRequestMessageCodec extends TextStreamCodecAdapter<SyncRequestMessage> {

    @Override
    public SyncRequestMessage decode(Reader reader) throws DecodeException, IOException {

        try (JsonReader jsonReader = Json.createReader(reader)) {
            final JsonObject json = jsonReader.readObject();
            final String partyInfoData = json.getString("partyInfo");
            final PartyInfo partyInfo = MessageUtil.decodePartyInfoFromBase64(partyInfoData);

            return SyncRequestMessage.Builder.create().withPartyInfo(partyInfo).build();
        }
    }

    @Override
    public void encode(SyncRequestMessage object, Writer writer) throws EncodeException, IOException {

        JsonObject json =
                Json.createObjectBuilder().add("partyInfo", MessageUtil.encodeToBase64(object.getPartyInfo())).build();

        try (JsonWriter jsonWriter = Json.createWriter(writer)) {
            jsonWriter.writeObject(json);
        }
    }
}
