package com.jpmorgan.quorum.tessera.sync;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.jpmorgan.quorum.mock.websocket.MockContainerProvider;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.transaction.TransactionManager;
import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class TransactionPublisherTest {

    private TransactionPublisher transactionPublisher;

    private PartyInfoService partyInfoService;

    private TransactionManager transactionManager;

    private PartyInfoClientEndpoint partyInfoClientEndpoint;

    private WebSocketContainer container = MockContainerProvider.getInstance();

    @Before
    public void onSetUp() {
        partyInfoService = mock(PartyInfoService.class);
        transactionManager = mock(TransactionManager.class);
        partyInfoClientEndpoint = new PartyInfoClientEndpoint(partyInfoService, transactionManager);
        transactionPublisher = new TransactionPublisher(partyInfoService, partyInfoClientEndpoint);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(partyInfoService);
    }

    @Test
    public void publish() throws Exception {

        final EncodedPayload encodedPayload = Fixtures.samplePayload();
        PublicKey recipientKey = Fixtures.sampleKey();

        when(partyInfoService.getUrlsForKey(recipientKey))
                .thenReturn(Stream.of("ws://one.com/sync", "ws://two.com/sync").collect(Collectors.toSet()));

        Session session = mock(Session.class);
        Basic basicRemote = mock(Basic.class);
        when(session.getBasicRemote()).thenReturn(basicRemote);

        when(container.connectToServer(partyInfoClientEndpoint, URI.create("ws://one.com/sync"))).thenReturn(session);
        when(container.connectToServer(partyInfoClientEndpoint, URI.create("ws://two.com/sync"))).thenReturn(session);

        transactionPublisher.publishPayload(encodedPayload, recipientKey);

        verify(basicRemote, times(2)).sendObject(any());
        verify(partyInfoService).getUrlsForKey(recipientKey);
    }

    @Test
    public void constructWithNoArgs() {

        Set services = Stream.of(partyInfoService, transactionManager).collect(Collectors.toSet());

        MockServiceLocator.createMockServiceLocator().setServices(services);

        assertThat(new TransactionPublisher()).isNotNull();
    }
}
