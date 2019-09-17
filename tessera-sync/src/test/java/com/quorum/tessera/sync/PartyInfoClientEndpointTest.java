package com.quorum.tessera.sync;

import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.transaction.TransactionManager;
import java.net.URI;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class PartyInfoClientEndpointTest {

    private PartyInfoClientEndpoint partyInfoClientEndpoint;

    private PartyInfoService partyInfoService;

    private TransactionManager transactionManager;

    @Before
    public void onSetUp() {
        transactionManager = mock(TransactionManager.class);
        partyInfoService = mock(PartyInfoService.class);
        partyInfoClientEndpoint = new PartyInfoClientEndpoint(partyInfoService, transactionManager);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(partyInfoService, transactionManager);
    }

    @Test
    public void onOpen() {
        Session session = mock(Session.class);
        partyInfoClientEndpoint.onOpen(session);
    }

    @Test
    public void onPartyInfoResponse() throws Exception {

        PartyInfo requestedPartyInfo = mock(PartyInfo.class);

        SyncResponseMessage syncResponseMessage =
                SyncResponseMessage.Builder.create(SyncResponseMessage.Type.PARTY_INFO)
                        .withPartyInfo(requestedPartyInfo)
                        .build();

        Session session = mock(Session.class);
        partyInfoClientEndpoint.onResponse(session, syncResponseMessage);
    };

    @Test
    public void onClose() {
        Session session = mock(Session.class);
        when(session.getRequestURI()).thenReturn(URI.create("ws://bogus.com:898/sync"));
        CloseReason reason = new CloseReason(CloseCodes.CANNOT_ACCEPT, "WHAT YOU TALKIN' ABOUT WILLIS?");
        partyInfoClientEndpoint.onClose(session, reason);
        verify(partyInfoService).removeRecipient("ws://bogus.com:898");
    }

    @Test
    public void onError() {
        partyInfoClientEndpoint.onError(new Exception("Ouch"));
    }

    // @Test
    public void openAndThenSendPartyInfo() throws Exception {

        final Session session = mock(Session.class);
        Basic publisher = mock(Basic.class);
        when(session.getBasicRemote()).thenReturn(publisher);

        partyInfoClientEndpoint.onOpen(session);
        PartyInfo samplePartyInfo = Fixtures.samplePartyInfo();

        PartyInfo requestedPartyInfo = mock(PartyInfo.class);

        when(partyInfoService.updatePartyInfo(requestedPartyInfo)).thenReturn(samplePartyInfo);

        SyncResponseMessage syncResponseMessage =
                SyncResponseMessage.Builder.create(SyncResponseMessage.Type.PARTY_INFO)
                        // .withPartyInfo(requestedPartyInfo)
                        .build();

        partyInfoClientEndpoint.onResponse(session, syncResponseMessage);

        // verify(partyInfoService).updatePartyInfo(requestedPartyInfo);
        // verify(publisher).sendObject(any());
    }
}
