package com.quorum.tessera.sync;

import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import java.net.URI;
import java.util.*;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

public class WebSocketPartyInfoPollerTest {

    private WebSocketPartyInfoPoller webSocketPartyInfoPoller;

    private PartyInfoService partyInfoService;

    private WebSocketContainer container;

    private PartyInfo partyInfo;

    @Before
    public void onSetUp() {

        Set<Party> parties = new HashSet<>();
        parties.add(new Party("ws://first.com"));
        parties.add(new Party("ws://second.com"));

        partyInfo = new PartyInfo("ws://localhost", Collections.emptySet(), parties);

        container = ContainerProvider.getWebSocketContainer();

        partyInfoService = mock(PartyInfoService.class);
        webSocketPartyInfoPoller = new WebSocketPartyInfoPoller(partyInfoService);

        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(partyInfoService, container);
        reset(container);
    }

    @Test
    public void poll() throws Exception {

        Session session = mock(Session.class);
        Basic basic = mock(Basic.class);
        when(session.getBasicRemote()).thenReturn(basic);

        List<SyncRequestMessage> requestMessages = new ArrayList<>();
        Mockito.doAnswer(
                        (iom) -> {
                            requestMessages.add(iom.getArgument(0));
                            return null;
                        })
                .when(basic)
                .sendObject(any(SyncRequestMessage.class));

        when(container.connectToServer(any(PartyInfoClientEndpoint.class), any(URI.class))).thenReturn(session);

        webSocketPartyInfoPoller.run();

        assertThat(requestMessages).hasSize(2);

        assertThat(requestMessages.get(0).getPartyInfo()).isSameAs(partyInfo);
        assertThat(requestMessages.get(1).getPartyInfo()).isSameAs(partyInfo);

        verify(partyInfoService).getPartyInfo();

        verify(container, times(2)).connectToServer(any(PartyInfoClientEndpoint.class), any(URI.class));

        webSocketPartyInfoPoller.clearSessions();

        verify(session, times(2)).close(any(CloseReason.class));
    }

    @Test
    public void pollEncodeExceptionIgnored() throws Exception {

        Session session = mock(Session.class);
        Basic basic = mock(Basic.class);
        when(session.getBasicRemote()).thenReturn(basic);

        doThrow(UncheckedWebSocketException.class).when(basic).sendObject(any());

        when(container.connectToServer(any(PartyInfoClientEndpoint.class), any(URI.class))).thenReturn(session);

        webSocketPartyInfoPoller.run();

        verify(partyInfoService).getPartyInfo();

        verify(container, times(2)).connectToServer(any(PartyInfoClientEndpoint.class), any(URI.class));

        webSocketPartyInfoPoller.clearSessions();
    }
}
