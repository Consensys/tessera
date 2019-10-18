package com.quorum.tessera.sync;

import com.jpmorgan.quorum.mock.websocket.MockContainerProvider;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadBuilder;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.PartyInfoParser;
import com.quorum.tessera.partyinfo.ResendRequest;
import com.quorum.tessera.partyinfo.ResendRequestType;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.*;
import java.net.URI;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class WebSocketP2pClientTest {

    private WebSocketP2pClient wsClient;

    private PartyInfoParser partyInfoParser;

    @Before
    public void setUp() {
        partyInfoParser = mock(PartyInfoParser.class);
        this.wsClient = new WebSocketP2pClient(partyInfoParser);
    }

    @Test
    public void testPush() throws Exception {

        WebSocketContainer mockWebSocketContainer = MockContainerProvider.getInstance();

        final EncodedPayload encodedPayload =
                EncodedPayloadBuilder.create()
                        .withSenderKey(PublicKey.from("key".getBytes()))
                        .withCipherText("cipherText".getBytes())
                        .withCipherTextNonce("cipherTextNonce".getBytes())
                        .withRecipientBoxes(Collections.singletonList("recipientBox".getBytes()))
                        .withRecipientKeys(PublicKey.from("recipientKey".getBytes()))
                        .withRecipientNonce("recipientNonce".getBytes())
                        .build();

        final byte[] payload = PayloadEncoder.create().encode(encodedPayload);
        final String targetUrl = "ws://somenode.com";

        URI expectedUri = URI.create("ws://somenode.com/sync");

        Session session = mock(Session.class);
        RemoteEndpoint.Basic basic = mock(RemoteEndpoint.Basic.class);
        when(session.getBasicRemote()).thenReturn(basic);

        doAnswer(
                        (invocation) -> {
                            return null;
                        })
                .when(basic)
                .sendObject(any(SyncRequestMessage.class));

        when(mockWebSocketContainer.connectToServer(wsClient, expectedUri)).thenReturn(session);

        wsClient.push(targetUrl, payload);

        verify(mockWebSocketContainer).connectToServer(wsClient, expectedUri);
        verify(basic).sendObject(any(SyncRequestMessage.class));
        verify(session).getBasicRemote();

        verify(session).close();

        verifyNoMoreInteractions(mockWebSocketContainer, basic, session);
    }

    @Test
    public void testPartyInfo() throws Exception {

        WebSocketContainer mockWebSocketContainer = MockContainerProvider.getInstance();

        PartyInfo partyInfo = mock(PartyInfo.class);

        final byte[] partyInfoData = "PI".getBytes();

        when(partyInfoParser.from(partyInfoData)).thenReturn(partyInfo);
        when(partyInfoParser.to(partyInfo)).thenReturn(partyInfoData);

        final String targetUrl = "ws://somenode.com";

        URI expectedUri = URI.create("ws://somenode.com/sync");

        Session session = mock(Session.class);
        RemoteEndpoint.Basic basic = mock(RemoteEndpoint.Basic.class);
        when(session.getBasicRemote()).thenReturn(basic);

        doAnswer(
                        (invocation) -> {
                            return null;
                        })
                .when(basic)
                .sendObject(any(SyncRequestMessage.class));

        when(mockWebSocketContainer.connectToServer(wsClient, expectedUri)).thenReturn(session);

        wsClient.sendPartyInfo(targetUrl, partyInfoData);

        verify(mockWebSocketContainer).connectToServer(wsClient, expectedUri);
        verify(basic).sendObject(any(SyncRequestMessage.class));
        verify(session).getBasicRemote();

        verify(session).close();

        verifyNoMoreInteractions(mockWebSocketContainer, basic, session);
    }

    @Test
    public void testResend() throws Exception {

        final WebSocketContainer mockWebSocketContainer = MockContainerProvider.getInstance();

        final String targetUrl = "ws://somenode.com";
        final URI expectedUri = URI.create("ws://somenode.com/sync");

        ResendRequest request = new ResendRequest();
        request.setPublicKey("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=");
        request.setType(ResendRequestType.ALL);

        Session session = mock(Session.class);
        RemoteEndpoint.Basic basic = mock(RemoteEndpoint.Basic.class);
        when(session.getBasicRemote()).thenReturn(basic);

        doAnswer(
                        (invocation) -> {
                            return null;
                        })
                .when(basic)
                .sendObject(any(SyncRequestMessage.class));

        when(mockWebSocketContainer.connectToServer(wsClient, expectedUri)).thenReturn(session);

        wsClient.makeResendRequest(targetUrl, request);

        verify(mockWebSocketContainer).connectToServer(wsClient, expectedUri);
        verify(basic).sendObject(any(SyncRequestMessage.class));
        verify(session).getBasicRemote();

        verify(session).close();

        verifyNoMoreInteractions(mockWebSocketContainer, basic, session);
    }

    @Test
    public void exceptionThrown() throws Exception {

        final WebSocketContainer mockWebSocketContainer = MockContainerProvider.getInstance();

        final String targetUrl = "ws://somenode.com";
        final URI expectedUri = URI.create("ws://somenode.com/sync");

        ResendRequest request = new ResendRequest();
        request.setPublicKey("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=");
        request.setType(ResendRequestType.ALL);

        Session session = mock(Session.class);
        RemoteEndpoint.Basic basic = mock(RemoteEndpoint.Basic.class);
        when(session.getBasicRemote()).thenReturn(basic);

        doThrow(UncheckedWebSocketException.class).when(basic).sendObject(any());

        when(mockWebSocketContainer.connectToServer(wsClient, expectedUri)).thenReturn(session);

        wsClient.makeResendRequest(targetUrl, request);

        verify(mockWebSocketContainer).connectToServer(wsClient, expectedUri);
        verify(basic).sendObject(any(SyncRequestMessage.class));
        verify(session).getBasicRemote();

        verify(session).close();

        verifyNoMoreInteractions(mockWebSocketContainer, basic, session);
    }
}
