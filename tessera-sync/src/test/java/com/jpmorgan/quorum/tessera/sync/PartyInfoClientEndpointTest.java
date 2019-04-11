package com.jpmorgan.quorum.tessera.sync;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.Session;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class PartyInfoClientEndpointTest {

    private PartyInfoClientEndpoint partyInfoClientEndpoint;

    @Before
    public void onSetUp() {
        partyInfoClientEndpoint = new PartyInfoClientEndpoint();
    }

    @Test
    public void onOpen() {
        Session session = mock(Session.class);
        partyInfoClientEndpoint.onOpen(session);
    }

    @Test
    public void onMEssage() {
        Session session = mock(Session.class);
        partyInfoClientEndpoint.onMessage(session, "HELLOW");
    }

    @Test
    public void onClose() {
        Session session = mock(Session.class);
        CloseReason reason = new CloseReason(CloseCodes.CANNOT_ACCEPT, "WHAT YOU TALKIN' ABOUT WILLIS?");
        partyInfoClientEndpoint.onClose(session, reason);
    }

}
