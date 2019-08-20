package com.quorum.tessera.sync;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
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

public class WebsocketPartyInfoPollerTest {

    private WebsocketPartyInfoPoller websocketPartyInfoPoller;

    private PartyInfoService partyInfoService;

    private Config config;

    private WebSocketContainer container;

    @Before
    public void onSetUp() {
        config = new Config();
        config.addPeer(new Peer("ws://first.com"));
        config.addPeer(new Peer("ws://second.com"));

        container = ContainerProvider.getWebSocketContainer();

        partyInfoService = mock(PartyInfoService.class);
        websocketPartyInfoPoller = new WebsocketPartyInfoPoller(config, partyInfoService);
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

        PartyInfo partyInfo = mock(PartyInfo.class);

        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);

        websocketPartyInfoPoller.run();

        assertThat(requestMessages).hasSize(2);

        assertThat(requestMessages.get(0).getPartyInfo()).isSameAs(partyInfo);
        assertThat(requestMessages.get(1).getPartyInfo()).isSameAs(partyInfo);

        verify(partyInfoService, times(2)).getPartyInfo();

        verify(container, times(2)).connectToServer(any(PartyInfoClientEndpoint.class), any(URI.class));

        websocketPartyInfoPoller.clearSessions();

        verify(session, times(2)).close(any(CloseReason.class));
    }

    @Test
    public void pollEncodeExceptionIgnored() throws Exception {

        Session session = mock(Session.class);
        Basic basic = mock(Basic.class);
        when(session.getBasicRemote()).thenReturn(basic);

        doThrow(UncheckedWebSocketException.class).when(basic).sendObject(any());

        when(container.connectToServer(any(PartyInfoClientEndpoint.class), any(URI.class))).thenReturn(session);

        PartyInfo partyInfo = mock(PartyInfo.class);

        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);

        websocketPartyInfoPoller.run();

        verify(partyInfoService, times(2)).getPartyInfo();

        verify(container, times(2)).connectToServer(any(PartyInfoClientEndpoint.class), any(URI.class));

        websocketPartyInfoPoller.clearSessions();

        verify(session, times(2)).close(any(CloseReason.class));
    }
}
