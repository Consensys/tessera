package com.jpmorgan.quorum.tessera.sync;

import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
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

    @Before
    public void onSetUp() {

        partyInfoService = mock(PartyInfoService.class);

        partyInfoEndpoint = new PartyInfoEndpoint(partyInfoService);
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

        PartyInfo partyInfo = mock(PartyInfo.class);

        PartyInfo updatedPartyInfo = mock(PartyInfo.class);

        when(partyInfoService.updatePartyInfo(partyInfo)).thenReturn(updatedPartyInfo);

        PartyInfo result = partyInfoEndpoint.onSync(session, partyInfo);

        assertThat(result).isNotNull().isSameAs(updatedPartyInfo);

        verify(partyInfoService).updatePartyInfo(partyInfo);
    }
}
