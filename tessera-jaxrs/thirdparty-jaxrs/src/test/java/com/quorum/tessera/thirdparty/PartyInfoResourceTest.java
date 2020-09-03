package com.quorum.tessera.thirdparty;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Recipient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PartyInfoResourceTest {

    private Discovery partyInfoService;

    private PartyInfoResource partyInfoResource;

    @Before
    public void onSetup() {
        this.partyInfoService = mock(Discovery.class);

        this.partyInfoResource = new PartyInfoResource(partyInfoService);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(partyInfoService);
    }

    @Test
    public void getPartyInfoKeys() {

        final String partyInfoJson =
                "{\"keys\":[{\"key\":\"BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=\"},{\"key\":\"QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=\"}]}";

        final Party someParty = new Party("http://localhost:9006/");
        final Party someOtherParty = new Party("http://localhost:9005/");

        final NodeInfo nodeInfo = NodeInfo.Builder.create()
            .withUrl("http://localhost:9001/")
            .withParties(List.of(someOtherParty, someParty))
            .withRecipients(List.of(
                Recipient.of(
                    PublicKey.from(
                        Base64.getDecoder()
                            .decode(
                                "QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=")),
                    "http://localhost:9002/"),
                Recipient.of(
                PublicKey.from(
                    Base64.getDecoder()
                        .decode(
                            "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=")),
                "http://localhost:9001/")))
            .build();

        when(partyInfoService.getCurrent()).thenReturn(nodeInfo);

        final Response response = partyInfoResource.getPartyInfoKeys();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final String output = response.getEntity().toString();
        final JsonReader expected = Json.createReader(new StringReader(partyInfoJson));
        final JsonReader actual = Json.createReader(new StringReader(output));

        JsonObject expectedJsonObject = expected.readObject();
        JsonObject actualJsonObject = actual.readObject();

        assertThat(actualJsonObject).containsOnlyKeys("keys");
        assertThat(actualJsonObject.getJsonArray("keys"))
            .containsExactlyInAnyOrderElementsOf(expectedJsonObject.getJsonArray("keys"));

        verify(partyInfoService).getCurrent();
    }
}
