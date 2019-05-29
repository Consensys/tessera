package com.quorum.tessera.grpc.p2p;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import com.quorum.tessera.partyinfo.PartyInfoParser;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.model.Recipient;
import com.quorum.tessera.encryption.PublicKey;
import io.grpc.stub.StreamObserver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class PartyInfoGrpcServiceTest {

    @Mock
    private PartyInfoService partyInfoService;

    @Mock
    private PartyInfoParser partyInfoParser;

    @Mock
    private StreamObserver<PartyInfoMessage> streamObserver;

    private PartyInfoGrpcService service;

    @Before
    public void onSetup() {
        MockitoAnnotations.initMocks(this);
        service = new PartyInfoGrpcService(partyInfoService, partyInfoParser);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(partyInfoService,partyInfoParser, streamObserver);
    }

    @Test
    public void testPartyInfo() {

        byte[] data = "{}".getBytes();

        final PartyInfoMessage partyInfoMessage = PartyInfoMessage.newBuilder()
            .setPartyInfo(ByteString.copyFrom(data))
            .build();

        PartyInfo partyInfo = mock(PartyInfo.class);
        when(partyInfoParser.from(data)).thenReturn(partyInfo);
        when(partyInfoService.updatePartyInfo(partyInfo)).thenReturn(partyInfo);

        byte[] resultData = "I LOVE SPARROWS!!".getBytes();

        when(partyInfoParser.to(partyInfo)).thenReturn(resultData);

        service.getPartyInfo(partyInfoMessage, streamObserver);

        ArgumentCaptor<PartyInfoMessage> responseCaptor = ArgumentCaptor.forClass(PartyInfoMessage.class);
        verify(streamObserver).onNext(responseCaptor.capture());
        PartyInfoMessage response = responseCaptor.getValue();

        assertThat(response).isNotNull();
        assertThat(response.getPartyInfo().toByteArray()).isEqualTo(resultData);

        verify(partyInfoParser).from(data);
        verify(partyInfoService).updatePartyInfo(partyInfo);
        verify(partyInfoParser).to(partyInfo);

        verify(streamObserver).onCompleted();

    }

    @Test
    public void partyInfoJsonGet() {

        final StreamObserver<PartyInfoJson> streamObserver = mock(StreamObserver.class);

        final Map<String, String> expectedKeys = new HashMap<>();
        expectedKeys.put("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=", "http://localhost:9001/");
        expectedKeys.put("QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=", "http://localhost:9002/");

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

        ///

        service.getPartyInfoMessage(Empty.getDefaultInstance(), streamObserver);

        ArgumentCaptor<PartyInfoJson> responseCaptor = ArgumentCaptor.forClass(PartyInfoJson.class);
        verify(streamObserver).onNext(responseCaptor.capture());
        PartyInfoJson response = responseCaptor.getValue();

        assertThat(response).isNotNull();
        assertThat(response.getUrl()).isEqualTo("http://localhost:9001/");
        assertThat(response.getPeersList().iterator()).containsExactlyInAnyOrder(
            Peer.newBuilder().setUrl("http://localhost:9006/")
                .build(),
            Peer.newBuilder().setUrl("http://localhost:9005/")
                .setUtcTimestamp(Timestamp.newBuilder().setSeconds(1546441402).build())
                .build()
        );
        assertThat(response.getKeysMap()).containsAllEntriesOf(expectedKeys);

        verify(partyInfoService).getPartyInfo();
        verify(streamObserver).onCompleted();

    }
}
