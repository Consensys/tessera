package com.jpmorgan.quorum.tessera.sync;

import com.quorum.tessera.node.model.PartyInfo;
import java.util.UUID;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PartyInfoEndpointTest {

    private PartyInfoEndpoint partyInfoEndpoint;

    private Session session;

    @Before
    public void onSetUp() {
        partyInfoEndpoint = new PartyInfoEndpoint();
        session = mock(Session.class);
        when(session.getId()).thenReturn(UUID.randomUUID().toString());
    }

    @Test
    public void onOpenAndThenClose() {

        partyInfoEndpoint.onOpen(session);

        assertThat(partyInfoEndpoint.getSessions()).containsOnly(session);

        partyInfoEndpoint.onClose(session);
        assertThat(partyInfoEndpoint.getSessions()).isEmpty();

    }

    @Test
    public void onSync() {

        Async aync = mock(Async.class);
        when(session.getAsyncRemote()).thenReturn(aync);

        PartyInfo partyInfo = mock(PartyInfo.class);
        partyInfoEndpoint.onSync(session, partyInfo);

        verify(aync).sendText("ACK");
        verify(session).getAsyncRemote();

    }

}
