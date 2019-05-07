package com.quorum.tessera.p2p;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.node.PartyInfoParser;
import com.quorum.tessera.node.PartyInfoService;
import com.quorum.tessera.node.model.Party;
import com.quorum.tessera.node.model.PartyInfo;
import com.quorum.tessera.node.model.Recipient;
import java.io.OutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonReader;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.StreamingOutput;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PartyInfoResourceTest {

    private PartyInfoService partyInfoService;

    private PartyInfoResource partyInfoResource;

    private PartyInfoParser partyInfoParser;

    private Enclave enclave;

    private Client restClient;

    private PayloadEncoder payloadEncoder;

    @Before
    public void onSetup() {
        this.partyInfoService = mock(PartyInfoService.class);
        this.partyInfoParser = mock(PartyInfoParser.class);
        this.enclave = mock(Enclave.class);
        this.restClient = mock(Client.class);
        this.payloadEncoder = mock(PayloadEncoder.class);
        this.partyInfoResource = new PartyInfoResource(partyInfoService, partyInfoParser, restClient, enclave, payloadEncoder);

    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(partyInfoService, partyInfoParser, restClient, enclave, payloadEncoder);
    }

    @Test
    public void partyInfoGet() {

        final String partyInfoJson = "{\"url\":\"http://localhost:9001/\",\"peers\":[{\"url\":\"http://localhost:9006/\",\"lastContact\":null},{\"url\":\"http://localhost:9005/\",\"lastContact\":\"2019-01-02T15:03:22.875Z\"}],\"keys\":[{\"key\":\"BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=\",\"url\":\"http://localhost:9001/\"},{\"key\":\"QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=\",\"url\":\"http://localhost:9002/\"}]}";

        final Party partyWithoutTimestamp = new Party("http://localhost:9006/");
        final Party partyWithTimestamp = new Party("http://localhost:9005/");
        partyWithTimestamp.setLastContacted(Instant.parse("2019-01-02T15:03:22.875Z"));

        final PartyInfo partyInfo = new PartyInfo(
            "http://localhost:9001/",
            new HashSet<>(Arrays.asList(
                new Recipient(PublicKey.from(Base64.getDecoder().decode("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=")), "http://localhost:9001/"),
                new Recipient(PublicKey.from(Base64.getDecoder().decode("QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=")), "http://localhost:9002/"))
            ),
            new HashSet<>(Arrays.asList(partyWithTimestamp, partyWithoutTimestamp))
        );

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
    public void partyInfo() throws Exception {

        String url = "http://www.bogus.com";

        PublicKey myKey = PublicKey.from("myKey".getBytes());

        PublicKey recipientKey = PublicKey.from("recipientKey".getBytes());

        String message = "I love sparrows";

        byte[] payload = message.getBytes();

        Recipient recipient = new Recipient(recipientKey, url);

        Set<Recipient> recipientList = Collections.singleton(recipient);

        PartyInfo partyInfo = new PartyInfo(url, recipientList, Collections.EMPTY_SET);

        when(partyInfoParser.from(payload)).thenReturn(partyInfo);

        when(enclave.defaultPublicKey()).thenReturn(myKey);

        when(partyInfoParser.to(partyInfo)).thenReturn(payload);

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        List<String> uuidList = new ArrayList<>();
        doAnswer((invocation) -> {
            byte[] d = invocation.getArgument(0);
            uuidList.add(new String(d));
            return encodedPayload;
        }).when(enclave).encryptPayload(any(byte[].class), any(PublicKey.class), anyList());

        when(payloadEncoder.encode(encodedPayload)).thenReturn(payload);

        WebTarget webTarget = mock(WebTarget.class);
        when(restClient.target(url)).thenReturn(webTarget);
        when(webTarget.path(anyString())).thenReturn(webTarget);
        Invocation.Builder invocationBuilder = mock(Invocation.Builder.class);
        when(webTarget.request()).thenReturn(invocationBuilder);

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(200);

        doAnswer((invocation) -> uuidList.get(0)).when(response).readEntity(String.class);

        when(invocationBuilder.post(any(Entity.class))).thenReturn(response);

        when(partyInfoService.updatePartyInfo(any(PartyInfo.class))).thenReturn(partyInfo);

        Response result = partyInfoResource.partyInfo(payload);

        assertThat(result.getStatus()).isEqualTo(200);
        //Work around for jacoco's lambda coverage
        StreamingOutput o = (StreamingOutput) result.getEntity();
        o.write(mock(OutputStream.class));
        assertThat(o).isNotNull();


        verify(partyInfoParser).from(payload);
        verify(partyInfoParser).to(partyInfo);
        verify(enclave).defaultPublicKey();
        verify(enclave,times(2)).encryptPayload(any(byte[].class), any(PublicKey.class), anyList());
        verify(payloadEncoder,times(2)).encode(encodedPayload);
        verify(restClient,times(2)).target(url);
        verify(partyInfoService).updatePartyInfo(any(PartyInfo.class));

    }

    @Test
    public void validate() throws Exception {

        String message = "I love sparrows";

        byte[] payload = message.getBytes();

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(payloadEncoder.decode(payload)).thenReturn(encodedPayload);

        PublicKey myKey = PublicKey.from("myKey".getBytes());

        when(enclave.defaultPublicKey()).thenReturn(myKey);
        when(enclave.unencryptTransaction(encodedPayload, myKey)).thenReturn(message.getBytes());

        Response result = partyInfoResource.validate(payload);

        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.getEntity()).isEqualTo(message);

        verify(payloadEncoder).decode(payload);
        verify(enclave).defaultPublicKey();
        verify(enclave).unencryptTransaction(encodedPayload, myKey);

    }


    @Test
    public void partyInfoNoRecipientWithPartInfoUrl() throws Exception {

        String url = "http://www.bogus.com";

        PublicKey myKey = PublicKey.from("myKey".getBytes());

        PublicKey recipientKey = PublicKey.from("recipientKey".getBytes());

        String message = "I love sparrows";

        byte[] payload = message.getBytes();

        Recipient recipient = new Recipient(recipientKey, "http://other.com");

        Set<Recipient> recipientList = Collections.singleton(recipient);

        PartyInfo partyInfo = new PartyInfo(url, recipientList, Collections.EMPTY_SET);

        when(partyInfoParser.from(payload)).thenReturn(partyInfo);

        when(enclave.defaultPublicKey()).thenReturn(myKey);

        try {
            partyInfoResource.partyInfo(payload);
            failBecauseExceptionWasNotThrown(SecurityException.class);
        } catch (SecurityException ex) {
            verify(partyInfoParser).from(payload);
            verify(enclave).defaultPublicKey();
        }

    }

    @Test
    public void constructWithMinimalArgs() {
        PartyInfoResource instance = new PartyInfoResource(partyInfoService, partyInfoParser, restClient, enclave);

        assertThat(instance).isNotNull();

    }

    @Test
    public void partyInfoVaidateNodeFails() throws Exception {

        String url = "http://www.bogus.com";

        PublicKey myKey = PublicKey.from("myKey".getBytes());

        PublicKey recipientKey = PublicKey.from("recipientKey".getBytes());

        String message = "I love sparrows";

        byte[] payload = message.getBytes();

        Recipient recipient = new Recipient(recipientKey, url);

        Set<Recipient> recipientList = Collections.singleton(recipient);

        PartyInfo partyInfo = new PartyInfo(url, recipientList, Collections.EMPTY_SET);

        when(partyInfoParser.from(payload)).thenReturn(partyInfo);

        when(enclave.defaultPublicKey()).thenReturn(myKey);

        when(partyInfoParser.to(partyInfo)).thenReturn(payload);

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(enclave.encryptPayload(any(byte[].class), any(PublicKey.class), anyList())).thenReturn(encodedPayload);

        when(payloadEncoder.encode(encodedPayload)).thenReturn(payload);

        WebTarget webTarget = mock(WebTarget.class);
        when(restClient.target(url)).thenReturn(webTarget);
        when(webTarget.path(anyString())).thenReturn(webTarget);
        Invocation.Builder invocationBuilder = mock(Invocation.Builder.class);
        when(webTarget.request()).thenReturn(invocationBuilder);

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(200);

        doAnswer((invocation) -> "BADRESPONSE").when(response).readEntity(String.class);

        when(invocationBuilder.post(any(Entity.class))).thenReturn(response);

        when(partyInfoService.updatePartyInfo(partyInfo)).thenReturn(partyInfo);

        try {
            partyInfoResource.partyInfo(payload);
            failBecauseExceptionWasNotThrown(SecurityException.class);
        } catch (SecurityException ex) {
            verify(partyInfoParser).from(payload);
            verify(enclave).defaultPublicKey();
            verify(enclave).encryptPayload(any(byte[].class), any(PublicKey.class), anyList());
            verify(payloadEncoder).encode(encodedPayload);
            verify(restClient).target(url);
        }

    }
}
