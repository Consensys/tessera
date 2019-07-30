package com.quorum.tessera.sync;

import com.quorum.tessera.enclave.EncodedPayload;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebsocketPayloadPublisherTest {

    @Test
    public void publish() throws Exception {

        String targetUrl = "http://target.com";

        SessionStore sessionStore = mock(SessionStore.class);
        Session session = mock(Session.class);
        when(session.getRequestURI()).thenReturn(URI.create(targetUrl));

        Basic sessionPublisher = mock(Basic.class);
        when(session.getBasicRemote()).thenReturn(sessionPublisher);

        when(sessionStore.findByUri(URI.create(targetUrl))).thenReturn(session);

        WebsocketPayloadPublisher payloadPublisher = new WebsocketPayloadPublisher(sessionStore);

        EncodedPayload payload = mock(EncodedPayload.class);

        List<SyncRequestMessage> sent = new ArrayList<>();
        doAnswer((iom) -> {
            sent.add(iom.getArgument(0));
            return null;
        }).when(sessionPublisher).sendObject(any(SyncRequestMessage.class));

        payloadPublisher.publishPayload(payload, targetUrl);

        verify(sessionPublisher).sendObject(any(SyncRequestMessage.class));

        assertThat(sent).hasSize(1);

        SyncRequestMessage sentMessage = sent.get(0);
        assertThat(sentMessage.getType()).isEqualTo(SyncRequestMessage.Type.TRANSACTION_PUSH);
        assertThat(sentMessage.getTransactions()).isSameAs(payload);

    }


}
