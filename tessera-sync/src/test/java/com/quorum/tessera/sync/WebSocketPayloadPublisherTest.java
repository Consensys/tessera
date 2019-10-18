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
}
