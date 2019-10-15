package com.quorum.tessera.p2p;

import com.quorum.tessera.partyinfo.PartyInfoParser;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.PartyInfoValidatorCallback;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.json.Json;
import javax.json.JsonReader;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PartyInfoResourceTest {

    private PartyInfoService partyInfoService;

    private PartyInfoResource partyInfoResource;

    private PartyInfoParser partyInfoParser;

    private PartyInfoValidatorCallback partyInfoValidatorCallback;

    @Before
    public void onSetup() {
        this.partyInfoService = mock(PartyInfoService.class);
        this.partyInfoParser = mock(PartyInfoParser.class);
        this.partyInfoValidatorCallback = mock(PartyInfoValidatorCallback.class);
        this.partyInfoResource =
                new PartyInfoResource(partyInfoService, partyInfoParser, partyInfoValidatorCallback, true);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(partyInfoService, partyInfoParser, partyInfoValidatorCallback);
    }

    @Test
    public void partyInfoGet() {

        final String partyInfoJson =
                "{\"url\":\"http://localhost:9001/\",\"peers\":[{\"url\":\"http://localhost:9006/\",\"lastContact\":null},{\"url\":\"http://localhost:9005/\",\"lastContact\":\"2019-01-02T15:03:22.875Z\"}],\"keys\":[{\"key\":\"BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=\",\"url\":\"http://localhost:9001/\"},{\"key\":\"QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=\",\"url\":\"http://localhost:9002/\"}]}";

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

    @Test
    public void partyInfo() {

        String url = "http://www.bogus.com";

        PublicKey recipientKey = PublicKey.from("recipientKey".getBytes());

        String message = "I love sparrows";

        byte[] payload = message.getBytes();

        Recipient recipient = new Recipient(recipientKey, url);

        Set<Recipient> recipientList = Collections.singleton(recipient);

        PartyInfo partyInfo = new PartyInfo(url, recipientList, Collections.emptySet());

        when(partyInfoParser.from(payload)).thenReturn(partyInfo);

        when(partyInfoService.validateAndExtractValidRecipients(
                        any(PartyInfo.class), any(PartyInfoValidatorCallback.class)))
                .thenReturn(recipientList);

        when(partyInfoService.updatePartyInfo(any(PartyInfo.class))).thenReturn(partyInfo);

        Response result = partyInfoResource.partyInfo(payload);

        assertThat(result.getStatus()).isEqualTo(200);

        verify(partyInfoParser).from(payload);
        verify(partyInfoService).updatePartyInfo(any(PartyInfo.class));
        verify(partyInfoService)
                .validateAndExtractValidRecipients(any(PartyInfo.class), any(PartyInfoValidatorCallback.class));
    }

    @Test
    public void validate() {

        String message = "I love sparrows";

        byte[] payload = message.getBytes();

        when(partyInfoService.unencryptSampleData(payload)).thenReturn("So do I".getBytes());

        Response result = partyInfoResource.validate(payload);

        verify(partyInfoService).unencryptSampleData(payload);

        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getEntity()).isEqualTo("So do I");
    }

    @Test
    public void constructWithMinimalArgs() {
        PartyInfoResource instance =
                new PartyInfoResource(partyInfoService, partyInfoParser, partyInfoValidatorCallback, true);
        assertThat(instance).isNotNull();
    }

    @Test
    public void partyInfoValidateThrowsException() {

        String message = "I love sparrows";

        byte[] payload = message.getBytes();

        PartyInfo partyInfo = mock(PartyInfo.class);
        when(partyInfoParser.from(payload)).thenReturn(partyInfo);

        when(partyInfoService.validateAndExtractValidRecipients(
                        any(PartyInfo.class), any(PartyInfoValidatorCallback.class)))
                .thenReturn(Collections.EMPTY_SET);

        try {
            partyInfoResource.partyInfo(payload);
            failBecauseExceptionWasNotThrown(SecurityException.class);
        } catch (SecurityException ex) {
            verify(partyInfoService)
                    .validateAndExtractValidRecipients(any(PartyInfo.class), any(PartyInfoValidatorCallback.class));
            verify(partyInfoParser).from(payload);
        }
    }

    @Test
    public void validationDisabledPassesAllKeysToStore() {
        this.partyInfoResource =
                new PartyInfoResource(partyInfoService, partyInfoParser, partyInfoValidatorCallback, false);

        final byte[] payload = "Test message".getBytes();

        final String url = "http://www.bogus.com";
        final String otherurl = "http://www.randomaddress.com";
        final PublicKey recipientKey = PublicKey.from("recipientKey".getBytes());
        final Set<Recipient> recipientList =
                new HashSet<>(Arrays.asList(new Recipient(recipientKey, url), new Recipient(recipientKey, otherurl)));
        final PartyInfo partyInfo = new PartyInfo(url, recipientList, Collections.emptySet());

        final ArgumentCaptor<PartyInfo> captor = ArgumentCaptor.forClass(PartyInfo.class);
        final byte[] serialisedData = "SERIALISED".getBytes();

        when(partyInfoParser.from(payload)).thenReturn(partyInfo);
        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);
        when(partyInfoParser.to(captor.capture())).thenReturn(serialisedData);

        final Response callResponse = partyInfoResource.partyInfo(payload);
        final byte[] data = (byte[]) callResponse.getEntity();

        assertThat(captor.getValue().getUrl()).isEqualTo(url);
        assertThat(captor.getValue().getRecipients()).isEmpty();
        assertThat(captor.getValue().getParties()).isEmpty();
        assertThat(new String(data)).isEqualTo("SERIALISED");
        verify(partyInfoParser).from(payload);
        verify(partyInfoParser).to(any(PartyInfo.class));
        verify(partyInfoService).updatePartyInfo(partyInfo);
        verify(partyInfoService).getPartyInfo();
    }
}
