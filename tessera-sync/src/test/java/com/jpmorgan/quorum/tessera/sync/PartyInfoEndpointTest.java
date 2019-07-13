package com.jpmorgan.quorum.tessera.sync;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.transaction.EncryptedTransactionDAO;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import static org.assertj.core.api.Assertions.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class PartyInfoEndpointTest {

    private PartyInfoEndpoint partyInfoEndpoint;

    private Session session;

    private PartyInfoService partyInfoService;

    private EncryptedTransactionDAO encryptedTransactionDAO;

    private Enclave enclave;

    @Before
    public void onSetUp() {

        enclave = mock(Enclave.class);

        partyInfoService = mock(PartyInfoService.class);

        encryptedTransactionDAO = mock(EncryptedTransactionDAO.class);

        partyInfoEndpoint = new PartyInfoEndpoint(partyInfoService, encryptedTransactionDAO, enclave);
        session = mock(Session.class);
        when(session.getId()).thenReturn(UUID.randomUUID().toString());
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(partyInfoService);
    }

    @Test
    public void onOpenAndThenClose() throws URISyntaxException {

        String uri = "http://somedomain.com";

        when(session.getRequestURI()).thenReturn(new URI(uri));

        partyInfoEndpoint.onOpen(session);

        assertThat(partyInfoEndpoint.getSessions()).containsOnly(session);

        partyInfoEndpoint.onClose(session);
        assertThat(partyInfoEndpoint.getSessions()).isEmpty();

        verify(partyInfoService).removeRecipient(uri);
    }

    @Test
    public void onSync() throws Exception {

        PartyInfo partyInfo = Fixtures.samplePartyInfo();

        when(partyInfoService.updatePartyInfo(partyInfo)).thenReturn(partyInfo);

        SyncRequestMessage syncRequestMessage =
                SyncRequestMessage.Builder.create(SyncRequestMessage.Type.PARTY_INFO).withPartyInfo(partyInfo).build();

        Basic basic = mock(Basic.class);
        when(session.getBasicRemote()).thenReturn(basic);

        partyInfoEndpoint.onSync(session, syncRequestMessage);

        verify(basic).sendObject(any(SyncResponseMessage.class));
        verify(partyInfoService).updatePartyInfo(partyInfo);
    }
}
