package com.quorum.tessera.thirdparty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Recipient;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.ws.rs.core.Response;
import java.io.StringReader;
import java.util.Base64;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PartyInfoResourceTest {

  private Discovery discovery;

  private PartyInfoResource partyInfoResource;

  @Before
  public void onSetup() {
    this.discovery = mock(Discovery.class);

    this.partyInfoResource = new PartyInfoResource(discovery);
  }

  @After
  public void onTearDown() {
    verifyNoMoreInteractions(discovery);
  }

  @Test
  public void getPartyInfoKeys() {

    final String partyInfoJson =
        "{\"keys\":[{\"key\":\"BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=\"},{\"key\":\"QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=\"}]}";

    final NodeInfo nodeInfo =
        NodeInfo.Builder.create()
            .withUrl("http://localhost:9001/")
            .withRecipients(
                List.of(
                    Recipient.of(
                        PublicKey.from(
                            Base64.getDecoder()
                                .decode("QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=")),
                        "http://localhost:9002/"),
                    Recipient.of(
                        PublicKey.from(
                            Base64.getDecoder()
                                .decode("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=")),
                        "http://localhost:9001/")))
            .build();

    when(discovery.getCurrent()).thenReturn(nodeInfo);

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

    verify(discovery).getCurrent();
  }
}
