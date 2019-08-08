package com.quorum.tessera.sync;

import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.transaction.TransactionManager;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
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

    @Test
    public void onError() {
        partyInfoClientEndpoint.onError(new Exception("Ouch"));
    }
}
