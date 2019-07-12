package com.jpmorgan.quorum.tessera.sync;

import static com.jpmorgan.quorum.tessera.sync.Fixtures.*;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import java.io.StringReader;
import java.io.StringWriter;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
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
    public void encode() throws Exception {

        PartyInfo samplePartyInfo = samplePartyInfo();
        
        EncodedPayload sampleTransactions = samplePayload();
        
        SyncResponseMessage syncResponseMessage = SyncResponseMessage.Builder.create()
                .withPartyInfo(samplePartyInfo)
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
                
                String expectedEncodedPartyInfo = MessageUtil.encodeToBase64(samplePartyInfo);
                String expectedTransactions = MessageUtil.encodeToBase64(sampleTransactions);
                
                assertThat(jsonObject.getString("partyInfo"))
                        .isEqualTo(expectedEncodedPartyInfo);
                
                assertThat(jsonObject.getString("transactions"))
                        .isEqualTo(expectedTransactions);
                
            }
        }

    }

    @Test
    public void decode() throws Exception {

        PartyInfo partyInfo = samplePartyInfo();

        String encodedPartyInfo = MessageUtil.encodeToBase64(partyInfo);

        EncodedPayload payload = samplePayload();

        String transactionData = MessageUtil.encodeToBase64(payload);

        JsonObject json = Json.createObjectBuilder()
                .add("partyInfo", encodedPartyInfo)
                .add("transactionCount", 10)
                .add("transactionOffset", 9)
                .add("transactions", transactionData)
                .build();

        SyncResponseMessage result = syncResponseMessageCodec.decode(new StringReader(json.toString()));

        assertThat(result).isNotNull();
        assertThat(result.getTransactionCount()).isEqualTo(10L);
        assertThat(result.getTransactionOffset()).isEqualTo(9L);

        assertThat(refEq(result.getTransactions())).isEqualTo(refEq(payload));
        assertThat(refEq(result.getPartyInfo())).isEqualTo(refEq(partyInfo));

    }



}
