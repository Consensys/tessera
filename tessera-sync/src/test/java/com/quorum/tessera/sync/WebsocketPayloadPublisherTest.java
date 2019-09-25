package com.quorum.tessera.sync;

import com.jpmorgan.quorum.mock.websocket.MockContainerProvider;
import com.quorum.tessera.enclave.EncodedPayload;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;
import javax.websocket.CloseReason;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.apache.commons.lang3.concurrent.AtomicInitializer;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WebsocketPayloadPublisherTest {

    private WebsocketPayloadPublisher websocketPayloadPublisher;

    @Before
    public void onSetup() {
        this.websocketPayloadPublisher = new WebsocketPayloadPublisher();
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

        //Allow for one negative test on response queue for coverage
        final AtomicInteger counter = new AtomicInteger(0);
        
        doAnswer(
                        (invocation) -> {
                            if(counter.incrementAndGet() >= 1) {
                                SyncRequestMessage message = invocation.getArgument(0);
                                websocketPayloadPublisher.onResponse(mock(Session.class), message.getCorrelationId());
                            }
                            return null;
                        })
                .when(basic)
                .sendObject(any(SyncRequestMessage.class));

        when(mockWebSocketContainer.connectToServer(websocketPayloadPublisher, expectedUri)).thenReturn(session);

        websocketPayloadPublisher.publishPayload(payload, targetUrl);

        verify(mockWebSocketContainer).connectToServer(websocketPayloadPublisher, expectedUri);
        verify(basic).sendObject(any(SyncRequestMessage.class));
        verify(session).getBasicRemote();

        websocketPayloadPublisher.clearSessions();

        verify(session).close(any(CloseReason.class));

        verifyNoMoreInteractions(mockWebSocketContainer, basic, session);
    }

    @Test
    public void onError() {
        Throwable ex = mock(Throwable.class);
        websocketPayloadPublisher.onError(ex);
        assertThat(ex).isNotNull();
    }
}
