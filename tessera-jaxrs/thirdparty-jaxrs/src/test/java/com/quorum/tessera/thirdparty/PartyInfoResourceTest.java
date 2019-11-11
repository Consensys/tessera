package com.quorum.tessera.thirdparty;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonReader;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PartyInfoResourceTest {

    private PartyInfoService partyInfoService;

    private PartyInfoResource partyInfoResource;

    @Before
    public void onSetup() {
        this.partyInfoService = mock(PartyInfoService.class);

        this.partyInfoResource = new PartyInfoResource(partyInfoService);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(partyInfoService);
    }

    @Test
    public void partyInfoGet() {

        final String partyInfoJson =
            "{\"url\":\"http://localhost:9001/\",\"keys\":[{\"key\":\"BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=\"},{\"key\":\"QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=\"}]}";

        final Party partyWithoutTimestamp = new Party("http://localhost:9006/");
        final Party partyWithTimestamp = new Party("http://localhost:9005/");
        partyWithTimestamp.setLastContacted(Instant.parse("2019-01-02T15:03:22.875Z"));

        final PartyInfo partyInfo =
                new PartyInfo(
                        "http://localhost:9001/",
                        new HashSet<>(
                                Arrays.asList(
                                        new Recipient(
                                                PublicKey.from(
                                                        Base64.getDecoder()
                                                                .decode(
                                                                        "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=")),
                                                "http://localhost:9001/"),
                                        new Recipient(
                                                PublicKey.from(
                                                        Base64.getDecoder()
                                                                .decode(
                                                                        "QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=")),
                                                "http://localhost:9002/"))),
                        new HashSet<>(Arrays.asList(partyWithTimestamp, partyWithoutTimestamp)));

        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);

        final Response response = partyInfoResource.getPartyInfo();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        final String output = response.getEntity().toString();
        final JsonReader expected = Json.createReader(new StringReader(partyInfoJson));
        final JsonReader actual = Json.createReader(new StringReader(output));

        assertThat(expected.readObject()).isEqualTo(actual.readObject());

        verify(partyInfoService).getPartyInfo();
    }
}
