package com.quorum.tessera.api.grpc;

import com.google.protobuf.ByteString;
import com.quorum.tessera.api.grpc.model.PartyInfoMessage;
import com.quorum.tessera.node.PartyInfoParser;
import com.quorum.tessera.node.PartyInfoService;
import com.quorum.tessera.node.model.PartyInfo;
import io.grpc.stub.StreamObserver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
}
