package com.quorum.tessera.sync;

import com.jpmorgan.quorum.mock.websocket.MockContainerProvider;
import com.quorum.tessera.enclave.EncodedPayload;
import java.net.URI;
import javax.websocket.CloseReason;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WebSocketPayloadPublisherTest {

    private WebSocketPayloadPublisher wsPayloadPublisher;

    @Before
    public void onSetup() {
        this.wsPayloadPublisher = new WebSocketPayloadPublisher();
    }

    @Test
    public void publish() throws Exception {

        WebSocketContainer mockWebSocketContainer = MockContainerProvider.getInstance();

        final EncodedPayload payload = mock(EncodedPayload.class);
        final String targetUrl = "ws://somenode.com";

        URI expectedUri = URI.create("ws://somenode.com/sync");

        Session session = mock(Session.class);
        Basic basic = mock(Basic.class);
        when(session.getBasicRemote()).thenReturn(basic);

        doAnswer(
                        (invocation) -> {
                            SyncRequestMessage message = invocation.getArgument(0);
                            wsPayloadPublisher.onResponse(mock(Session.class), message.getCorrelationId());
                            return null;
                        })
                .when(basic)
                .sendObject(any(SyncRequestMessage.class));

        when(mockWebSocketContainer.connectToServer(wsPayloadPublisher, expectedUri)).thenReturn(session);

        wsPayloadPublisher.publishPayload(payload, targetUrl);

        verify(mockWebSocketContainer).connectToServer(wsPayloadPublisher, expectedUri);
        verify(basic).sendObject(any(SyncRequestMessage.class));
        verify(session).getBasicRemote();

        wsPayloadPublisher.clearSessions();

        verify(session).close(any(CloseReason.class));

        verifyNoMoreInteractions(mockWebSocketContainer, basic, session);
    }

    @Test
    public void onError() {
        Throwable ex = mock(Throwable.class);
        wsPayloadPublisher.onError(ex);
        assertThat(ex).isNotNull();
    }

    @Test
    public void onClose() throws Exception {
        WebSocketContainer mockWebSocketContainer = MockContainerProvider.getInstance();

        final EncodedPayload payload = mock(EncodedPayload.class);

        final String targetUrl1 = "ws://node1.com";
        URI expectedUri1 = URI.create("ws://node1.com/sync");

        Session session1 = mock(Session.class);
        Basic basic = mock(Basic.class);
        when(session1.getBasicRemote()).thenReturn(basic);

        doAnswer(
                        (invocation) -> {
                            SyncRequestMessage message = invocation.getArgument(0);
                            wsPayloadPublisher.onResponse(mock(Session.class), message.getCorrelationId());
                            return null;
                        })
                .when(basic)
                .sendObject(any(SyncRequestMessage.class));

        when(mockWebSocketContainer.connectToServer(wsPayloadPublisher, expectedUri1)).thenReturn(session1);

        wsPayloadPublisher.publishPayload(payload, targetUrl1);

        final String targetUrl2 = "ws://node2.com";

        URI expectedUri2 = URI.create("ws://node2.com/sync");

        Session session2 = mock(Session.class);

        when(session2.getBasicRemote()).thenReturn(basic);

        doAnswer(
                        (invocation) -> {
                            SyncRequestMessage message = invocation.getArgument(0);
                            wsPayloadPublisher.onResponse(mock(Session.class), message.getCorrelationId());
                            return null;
                        })
                .when(basic)
                .sendObject(any(SyncRequestMessage.class));

        when(mockWebSocketContainer.connectToServer(wsPayloadPublisher, expectedUri2)).thenReturn(session2);

        wsPayloadPublisher.publishPayload(payload, targetUrl2);

        verify(mockWebSocketContainer).connectToServer(wsPayloadPublisher, expectedUri1);
        verify(session1).getBasicRemote();
        verify(mockWebSocketContainer).connectToServer(wsPayloadPublisher, expectedUri2);
        verify(basic, times(2)).sendObject(any(SyncRequestMessage.class));
        verify(session2).getBasicRemote();

        CloseReason reason = mock(CloseReason.class);
        wsPayloadPublisher.onClose(session1, reason);

        wsPayloadPublisher.clearSessions();

        verify(session2).close(any(CloseReason.class));

        verifyNoMoreInteractions(mockWebSocketContainer, basic, session1, session2);
    }
}
