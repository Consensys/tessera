package com.quorum.tessera.sync;

import static com.quorum.tessera.sync.Fixtures.*;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class SyncResponseMessageCodecTest {

    private SyncResponseMessageCodec syncResponseMessageCodec;

    @Before
    public void onSetUp() {
        this.syncResponseMessageCodec = new SyncResponseMessageCodec();
    }

    @Test
    public void encodePartyInfoMessage() throws Exception {

        PartyInfo samplePartyInfo = samplePartyInfo();

        SyncResponseMessage syncResponseMessage =
                SyncResponseMessage.Builder.create(SyncResponseMessage.Type.PARTY_INFO)
                        .withPartyInfo(samplePartyInfo)
                        .build();

        try (StringWriter writer = new StringWriter()) {

            syncResponseMessageCodec.encode(syncResponseMessage, writer);

            String result = writer.toString();

            try (JsonReader jsonReader = Json.createReader(new StringReader(result))) {
                JsonObject jsonObject = jsonReader.readObject();

                assertThat(jsonObject).isNotNull();

                String expectedEncodedPartyInfo = MessageUtil.encodeToBase64(samplePartyInfo);

                assertThat(jsonObject.getString("type")).isEqualTo(SyncResponseMessage.Type.PARTY_INFO.name());
                assertThat(jsonObject.getString("partyInfo")).isEqualTo(expectedEncodedPartyInfo);
            }
        }
    }

    @Test
    public void encodeTransactionSyncMessage() throws Exception {

        EncodedPayload sampleTransactions = samplePayload();

        SyncResponseMessage syncResponseMessage =
                SyncResponseMessage.Builder.create(SyncResponseMessage.Type.TRANSACTION_SYNC)
                        .withTransactionCount(99)
                        .withTransactionOffset(8)
                        .withTransactions(sampleTransactions)
                        .build();

        try (StringWriter writer = new StringWriter()) {

            syncResponseMessageCodec.encode(syncResponseMessage, writer);

            String result = writer.toString();

            try (JsonReader jsonReader = Json.createReader(new StringReader(result))) {
                JsonObject jsonObject = jsonReader.readObject();

                assertThat(jsonObject).isNotNull();

                assertThat(jsonObject.getJsonNumber("transactionCount").longValueExact()).isEqualTo(99);
                assertThat(jsonObject.getJsonNumber("transactionOffset").longValueExact()).isEqualTo(8);

                String expectedTransactions = MessageUtil.encodeToBase64(sampleTransactions);

                assertThat(jsonObject.getString("transactions")).isEqualTo(expectedTransactions);
            }
        }
    }

    @Test
    public void decodePartyInfoMessage() throws Exception {

        PartyInfo partyInfo = samplePartyInfo();

        String encodedPartyInfo = MessageUtil.encodeToBase64(partyInfo);

        JsonObject json =
                Json.createObjectBuilder()
                        .add("type", SyncResponseMessage.Type.PARTY_INFO.name())
                        .add("partyInfo", encodedPartyInfo)
                        .build();

        SyncResponseMessage result = syncResponseMessageCodec.decode(new StringReader(json.toString()));

        assertThat(result).isNotNull();

        assertThat(result.getPartyInfo().getUrl()).isEqualTo(partyInfo.getUrl());
        assertThat(result.getPartyInfo().getParties()).isEqualTo(partyInfo.getParties());
        assertThat(result.getPartyInfo().getRecipients()).isEqualTo(partyInfo.getRecipients());
    }

    @Test
    public void decodeTransactionSyncMessage() throws Exception {

        EncodedPayload payload = samplePayload();

        String transactionData = MessageUtil.encodeToBase64(payload);

        JsonObject json =
                Json.createObjectBuilder()
                        .add("type", SyncResponseMessage.Type.TRANSACTION_SYNC.name())
                        .add("transactionCount", 10)
                        .add("transactionOffset", 9)
                        .add("transactions", transactionData)
                        .build();

        SyncResponseMessage result = syncResponseMessageCodec.decode(new StringReader(json.toString()));

        assertThat(result).isNotNull();
        assertThat(result.toString()).isNotNull();
        assertThat(result.getTransactionCount()).isEqualTo(10L);
        assertThat(result.getTransactionOffset()).isEqualTo(9L);

        assertThat(result.getTransactions().getCipherText()).isEqualTo(payload.getCipherText());
        assertThat(result.getTransactions().getRecipientKeys()).isEqualTo(payload.getRecipientKeys());
    }

    @Test
    public void initAndDestoryDoNothing() {

        EndpointConfig endpointConfig =
                new EndpointConfig() {
                    @Override
                    public List<Class<? extends Encoder>> getEncoders() {
                        throw new UnsupportedOperationException(
                                "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
                    }

                    @Override
                    public List<Class<? extends Decoder>> getDecoders() {
                        throw new UnsupportedOperationException(
                                "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
                    }

                    @Override
                    public Map<String, Object> getUserProperties() {
                        throw new UnsupportedOperationException(
                                "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
                    }
                };

        syncResponseMessageCodec.init(endpointConfig);
        syncResponseMessageCodec.destroy();

        assertThat(syncResponseMessageCodec).isNotNull();
    }
}
