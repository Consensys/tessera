package com.quorum.tessera.p2p;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.RawTransaction;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.node.PartyInfoParser;
import com.quorum.tessera.node.PartyInfoService;
import com.quorum.tessera.node.model.Party;
import com.quorum.tessera.node.model.PartyInfo;
import com.quorum.tessera.node.model.Recipient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class PartyInfoResourceTest {

    private PartyInfoService partyInfoService;

    private PartyInfoResource partyInfoResource;

    private PartyInfoParser partyInfoParser;

    private Enclave enclave;
    
    private Client restClient;
    
    @Before
    public void onSetup() {
        this.partyInfoService = mock(PartyInfoService.class);
        this.partyInfoParser = mock(PartyInfoParser.class);
        this.enclave = mock(Enclave.class);
        this.restClient = mock(Client.class);

        
        
        this.partyInfoResource = new PartyInfoResource(partyInfoService, partyInfoParser,restClient,enclave);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(partyInfoService, partyInfoParser,restClient,enclave);
    }

    @Test
    public void partyInfoPost() throws IOException {

        byte[] encryptedResponse = "Beware of the hooded mackerel".getBytes();

        PublicKey key = PublicKey.from("SOMEKEYDATA".getBytes());
                
        WebTarget webTarget = mock(WebTarget.class);
        Builder builder = mock(Builder.class);
        when(builder.post(any())).thenReturn(Response.ok()
            .entity(Base64.getEncoder().encode(encryptedResponse))
            .build());
        when(restClient.target(anyString())).thenReturn(webTarget);
        when(webTarget.path(anyString())).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(builder);

        Response r = mock(Response.class);
        when(r.readEntity(String.class))
            .thenReturn(Base64.getEncoder().encodeToString(encryptedResponse));

        when(builder.post(any(Entity.class)))
            .thenReturn(r);
        
        
        byte[] data = "{}".getBytes();

        RawTransaction txn = mock(RawTransaction.class);
        when(txn.getEncryptedPayload()).thenReturn(encryptedResponse);
        when(enclave.encryptRawPayload(any(),any())).thenReturn(txn);


        PartyInfo partyInfo = mock(PartyInfo.class);
        when(partyInfo.getUrl()).thenReturn("http://junit.com");

        Recipient recipient = mock(Recipient.class);

        when(recipient.getUrl()).thenReturn("http://junit.com");
        when(recipient.getKey()).thenReturn(key);

        when(partyInfo.getRecipients()).thenReturn(Collections.singleton(recipient));

        when(partyInfoParser.from(data)).thenReturn(partyInfo);
        when(partyInfoService.updatePartyInfo(partyInfo)).thenReturn(partyInfo);

        byte[] resultData = "Returned Party Info Data".getBytes();

        when(partyInfoParser.to(partyInfo)).thenReturn(resultData);

        Response response = partyInfoResource.partyInfo(data);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);

        StreamingOutput o = (StreamingOutput) response.getEntity();
        o.write(mock(OutputStream.class));

        assertThat(o).isNotNull();

        verify(partyInfoParser).from(data);
        verify(partyInfoService).updatePartyInfo(partyInfo);
        verify(partyInfoParser).to(partyInfo);

        verify(restClient).target(anyString());
        verify(enclave).encryptRawPayload(any(),any());
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
    public void validate() {

        PublicKey sender = PublicKey.from("SOMEKEYDATA".getBytes());

        byte[] someData = "SOMEDATA".getBytes();
        String encodedData = Base64.getEncoder().encodeToString(someData);

        JsonObject jsonObject = Json.createObjectBuilder()
            .add("sender",sender.encodeToBase64())
            .add("data",encodedData).build();


        RawTransaction txn = mock(RawTransaction.class);
        when(txn.getEncryptedPayload()).thenReturn("SOME_ENCRYPTED_DATA".getBytes());

        when(enclave.encryptRawPayload(someData,sender)).thenReturn(txn);

        Response response = partyInfoResource.validate(jsonObject.toString());

        assertThat(response.getStatus()).isEqualTo(200);


        assertThat(response.getEntity()).isEqualTo(Base64.getEncoder().encodeToString("SOME_ENCRYPTED_DATA".getBytes()));

        verify(enclave).encryptRawPayload(someData,sender);

    }

    @Test
    public void partyInfoPostInvalidNode() throws IOException {


        byte[] encryptedResponse = "Beware of the hooded mackerel".getBytes();

        PublicKey key = PublicKey.from("SOMEKEYDATA".getBytes());

        WebTarget webTarget = mock(WebTarget.class);
        Builder builder = mock(Builder.class);
        when(builder.post(any())).thenReturn(Response.ok()
            .entity(Base64.getEncoder().encode(encryptedResponse))
            .build());
        when(restClient.target(anyString())).thenReturn(webTarget);
        when(webTarget.path(anyString())).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(builder);

        Response r = mock(Response.class);
        when(r.readEntity(String.class))
            .thenReturn(Base64.getEncoder().encodeToString("OTHER".getBytes()));

        when(builder.post(any(Entity.class)))
            .thenReturn(r);


        byte[] data = "{}".getBytes();

        RawTransaction txn = mock(RawTransaction.class);
        when(txn.getEncryptedPayload()).thenReturn(encryptedResponse);
        when(enclave.encryptRawPayload(any(),any())).thenReturn(txn);


        PartyInfo partyInfo = mock(PartyInfo.class);
        when(partyInfo.getUrl()).thenReturn("http://junit.com");

        Recipient recipient = mock(Recipient.class);

        when(recipient.getUrl()).thenReturn("http://junit.com");
        when(recipient.getKey()).thenReturn(key);

        when(partyInfo.getRecipients()).thenReturn(Collections.singleton(recipient));

        when(partyInfoParser.from(data)).thenReturn(partyInfo);
        when(partyInfoService.updatePartyInfo(partyInfo)).thenReturn(partyInfo);

        byte[] resultData = "Returned Party Info Data".getBytes();

        when(partyInfoParser.to(partyInfo)).thenReturn(resultData);

        try {
            partyInfoResource.partyInfo(data);
            failBecauseExceptionWasNotThrown(SecurityException.class);
        } catch(SecurityException ex) {

            verify(partyInfoParser).from(data);

            verify(restClient).target(anyString());
            verify(enclave).encryptRawPayload(any(),any());
        }


    }

}
