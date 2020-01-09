package com.jpmorgan.quorum.tessera.sync;

import org.junit.Before;
import org.junit.Test;
import static com.jpmorgan.quorum.tessera.sync.Fixtures.*;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SyncRequestMessageCodecTest {

    private SyncRequestMessageCodec syncRequestMessageCodec;

    @Before
    public void onSetup() {
        syncRequestMessageCodec = new SyncRequestMessageCodec();
    }

    @Test
    public void encodePartyInfo() throws Exception {

        PartyInfo samplePartyInfo = samplePartyInfo();

        SyncRequestMessage syncRequestMessage =
                SyncRequestMessage.Builder.create(SyncRequestMessage.Type.PARTY_INFO)
                        .withPartyInfo(samplePartyInfo)
                        .build();

        try (Writer writer = new StringWriter()) {

            syncRequestMessageCodec.encode(syncRequestMessage, writer);

            String resultData = writer.toString();

            try (JsonReader jsonReader = Json.createReader(new StringReader(resultData))) {

                JsonObject result = jsonReader.readObject();

                String expected = MessageUtil.encodeToBase64(samplePartyInfo);

                assertThat(result.getString("partyInfo")).isEqualTo(expected);
                assertThat(result.getString("type")).isEqualTo(SyncRequestMessage.Type.PARTY_INFO.name());
            }
        }
    }

    @Test
    public void encodeTransactions() throws Exception {

        EncodedPayload sampleTransactions = samplePayload();

        SyncRequestMessage syncRequestMessage =
                SyncRequestMessage.Builder.create(SyncRequestMessage.Type.TRANSACTION_PUSH)
                        .withTransactions(sampleTransactions)
                        .withRecipientKey(sampleKey())
                        .build();

        try (Writer writer = new StringWriter()) {

            syncRequestMessageCodec.encode(syncRequestMessage, writer);

            String resultData = writer.toString();

            try (JsonReader jsonReader = Json.createReader(new StringReader(resultData))) {

                JsonObject result = jsonReader.readObject();

                String expected = MessageUtil.encodeToBase64(sampleTransactions);

                assertThat(result.getString("recipientKey")).isEqualTo(sampleKey().encodeToBase64());
                assertThat(result.getString("transactions")).isEqualTo(expected);
                assertThat(result.getString("type")).isEqualTo(SyncRequestMessage.Type.TRANSACTION_PUSH.name());
            }
        }
    }

    @Test
    public void decodePartyInfo() throws Exception {

        PartyInfo samplePartyInfo = samplePartyInfo();

        String data =
                Json.createObjectBuilder()
                        .add("type", SyncRequestMessage.Type.PARTY_INFO.name())
                        .add("partyInfo", MessageUtil.encodeToBase64(samplePartyInfo))
                        .build()
                        .toString();

        try (Reader reader = new StringReader(data)) {

            SyncRequestMessage result = syncRequestMessageCodec.decode(reader);

            assertThat(result.getType()).isEqualTo(SyncRequestMessage.Type.PARTY_INFO);
            assertThat(result.getPartyInfo().getUrl()).isEqualTo(samplePartyInfo.getUrl());
        }
    }

    @Test
    public void decodeTransactions() throws Exception {

        EncodedPayload sampleTransactions = samplePayload();

        PublicKey recipientKey = PublicKey.from("HELLOW".getBytes());

        String data =
                Json.createObjectBuilder()
                        .add("type", SyncRequestMessage.Type.TRANSACTION_PUSH.name())
                        .add("recipientKey", recipientKey.encodeToBase64())
                        .add("transactions", MessageUtil.encodeToBase64(sampleTransactions))
                        .build()
                        .toString();

        try (Reader reader = new StringReader(data)) {

            SyncRequestMessage result = syncRequestMessageCodec.decode(reader);
            assertThat(result.getType()).isEqualTo(SyncRequestMessage.Type.TRANSACTION_PUSH);
            assertThat(result.getTransactions()).isNotNull();
            assertThat(result.getRecipientKey()).isEqualTo(recipientKey);
        }
    }
}
