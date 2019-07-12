package com.jpmorgan.quorum.tessera.sync;

import org.junit.Before;
import org.junit.Test;
import static com.jpmorgan.quorum.tessera.sync.Fixtures.*;
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
    public void encode() throws Exception {

        PartyInfo samplePartyInfo = samplePartyInfo();

        SyncRequestMessage syncRequestMessage = SyncRequestMessage.Builder.create()
                .withPartyInfo(samplePartyInfo)
                .build();

        try (Writer writer = new StringWriter()) {

            syncRequestMessageCodec.encode(syncRequestMessage, writer);

            String resultData = writer.toString();

            try (JsonReader jsonReader = Json.createReader(new StringReader(resultData))) {

                JsonObject result = jsonReader.readObject();

                String expected = MessageUtil.encodeToBase64(samplePartyInfo);

                assertThat(result.getString("partyInfo")).isEqualTo(expected);
            }
        }
    }

    @Test
    public void decode() throws Exception {

        PartyInfo samplePartyInfo = samplePartyInfo();
        
        String data = Json.createObjectBuilder().add("partyInfo", MessageUtil.encodeToBase64(samplePartyInfo)).build().toString();

        try (Reader reader = new StringReader(data)) {

            SyncRequestMessage result = syncRequestMessageCodec.decode(reader);

            assertThat(refEq(result)).isEqualTo(refEq(samplePartyInfo));
            
        }
    }

}
