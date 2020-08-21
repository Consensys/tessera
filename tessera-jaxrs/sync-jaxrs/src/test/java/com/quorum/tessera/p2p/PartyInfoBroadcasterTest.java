package com.quorum.tessera.p2p;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.discovery.NodeUri;
import com.quorum.tessera.partyinfo.P2pClient;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Party;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;

import javax.ws.rs.ProcessingException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class PartyInfoBroadcasterTest {

    private static final String OWN_URL = "http://own.com:8080/";

    private static final String TARGET_URL = "http://bogus.com:9878/";

    private static final String TARGET_URL_2 = "http://otherwebsite.com:9878/";

    private static final byte[] DATA = "BOGUS".getBytes();

    private Discovery discovery;

    private PartyInfoParser partyInfoParser;

    private PartyInfoBroadcaster partyInfoBroadcaster;

    private P2pClient p2pClient;

    private Executor executor;

    private PartyStore partyStore;

    private Set<NodeUri> connectedNodes;

    @Before
    public void setUp() {
        this.connectedNodes = new HashSet<>();
        this.discovery = mock(Discovery.class);
        this.partyInfoParser = mock(PartyInfoParser.class);
        this.p2pClient = mock(P2pClient.class);
        this.executor = mock(Executor.class);
        this.partyStore = mock(PartyStore.class);
        doAnswer(
            (InvocationOnMock invocation) -> {
                ((Runnable) invocation.getArguments()[0]).run();
                return null;
            }
        ).when(executor).execute(any(Runnable.class));

        when(partyInfoParser.to(any(PartyInfo.class))).thenReturn(DATA);

        this.partyInfoBroadcaster = new PartyInfoBroadcaster(discovery, partyInfoParser, p2pClient, executor,partyStore,connectedNodes);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(discovery, partyInfoParser, p2pClient,partyStore);
    }

    @Test
    public void run() {
        final NodeInfo partyInfo = NodeInfo.Builder.create()
            .withUrl(OWN_URL)
            .withParties(Set.of(new Party(TARGET_URL)))
            .build();

        when(discovery.getCurrent()).thenReturn(partyInfo);
        when(p2pClient.sendPartyInfo(TARGET_URL,DATA)).thenReturn(true);

        partyInfoBroadcaster.run();
        verify(partyStore).getParties();
        verify(discovery).getCurrent();
        verify(partyInfoParser).to(any(PartyInfo.class));
        verify(p2pClient).sendPartyInfo(TARGET_URL, DATA);
        assertThat(connectedNodes).containsExactly(NodeUri.create(TARGET_URL));
    }

    @Test
    public void testWhenURLIsOwn() {
        final NodeInfo partyInfo = NodeInfo.Builder.create()
            .withUrl(OWN_URL)
            .withParties(Set.of(new Party(OWN_URL)))
            .build();


        when(discovery.getCurrent()).thenReturn(partyInfo);
        when(partyInfoParser.to(any(PartyInfo.class))).thenReturn(DATA);
        when(p2pClient.sendPartyInfo(OWN_URL,DATA)).thenReturn(true);

        partyInfoBroadcaster.run();

        verify(partyStore).getParties();
        verify(partyInfoParser).to(any(PartyInfo.class));
        verify(discovery).getCurrent();
        assertThat(connectedNodes).isEmpty();

    }

    @Test
    public void exceptionThrowByPostDoesntBubble() {
        final Set<Party> parties = new HashSet<>(Arrays.asList(new Party(TARGET_URL), new Party(TARGET_URL_2)));
        final NodeInfo partyInfo = NodeInfo.Builder.create()
            .withUrl(OWN_URL)
            .withParties(parties)
            .build();

        doReturn(partyInfo).when(discovery).getCurrent();
        doThrow(UnsupportedOperationException.class).when(p2pClient).sendPartyInfo(TARGET_URL, DATA);

        final Throwable throwable = catchThrowable(partyInfoBroadcaster::run);

        assertThat(throwable).isNull();

        verify(partyStore).getParties();
        verify(p2pClient).sendPartyInfo(TARGET_URL, DATA);
        verify(p2pClient).sendPartyInfo(TARGET_URL_2, DATA);
        verify(discovery).getCurrent();
        verify(partyInfoParser).to(any(PartyInfo.class));
    }

    @Test
    public void constructWithMinimalArgs() {
        assertThat(new PartyInfoBroadcaster(p2pClient)).isNotNull();
    }

    @Test
    public void jaxRsProcessingExceptionRemovesNode() {
        ProcessingException processingException = new ProcessingException("OUCH");
        CompletionException completionException = new CompletionException(processingException);

        when(p2pClient.sendPartyInfo(anyString(),any(byte[].class)))
            .thenThrow(completionException);

        String uriData = "http://castalia.com";
        connectedNodes.add(NodeUri.create(uriData));

        partyInfoBroadcaster.pollSingleParty(uriData,"somebytes".getBytes());

        verify(discovery).onDisconnect(URI.create(uriData));
        verify(partyStore).remove(URI.create(uriData));
        verify(p2pClient).sendPartyInfo(anyString(),any(byte[].class));
        assertThat(connectedNodes).isEmpty();

    }

    @Test
    public void jaxRsProcessingExceptionBeforeRemoteNodeHasConnected() {
        ProcessingException processingException = new ProcessingException("OUCH");
        CompletionException completionException = new CompletionException(processingException);

        when(p2pClient.sendPartyInfo(anyString(),any(byte[].class)))
            .thenThrow(completionException);

        String uriData = "http://castalia.com";

        partyInfoBroadcaster.pollSingleParty(uriData,"somebytes".getBytes());

        verify(discovery).onDisconnect(URI.create(uriData));
        verify(p2pClient).sendPartyInfo(anyString(),any(byte[].class));

    }
}
