package com.jpmorgan.quorum.tessera.sync;

import com.jpmorgan.quorum.mock.websocket.MockContainerProvider;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class PartyInfoClientEndpointTest {

    private PartyInfoClientEndpoint partyInfoClientEndpoint;

    private WebSocketContainer container;

    private PartyInfoService partyInfoService;

    @Before
    public void onSetUp() {
        partyInfoService = mock(PartyInfoService.class);
        partyInfoClientEndpoint = new PartyInfoClientEndpoint(partyInfoService);
        container = MockContainerProvider.getInstance();
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(partyInfoService, container);
    }

    @Test
    public void onOpen() {
        Session session = mock(Session.class);
        partyInfoClientEndpoint.onOpen(session);
    }

    @Test
    public void onResponse() throws Exception {

        PartyInfo samplePartyInfo = Fixtures.samplePartyInfo();

        SyncResponseMessage syncResponseMessage =
                SyncResponseMessage.Builder.create(SyncResponseMessage.Type.PARTY_INFO)
                        .withPartyInfo(samplePartyInfo)
                        .build();

        Session session = mock(Session.class);
        partyInfoClientEndpoint.onResponse(session, syncResponseMessage);

        verify(partyInfoService).updatePartyInfo(any(PartyInfo.class));
    }

    @Test
    public void onClose() {
        Session session = mock(Session.class);
        CloseReason reason = new CloseReason(CloseCodes.CANNOT_ACCEPT, "WHAT YOU TALKIN' ABOUT WILLIS?");
        partyInfoClientEndpoint.onClose(session, reason);
    }
}
