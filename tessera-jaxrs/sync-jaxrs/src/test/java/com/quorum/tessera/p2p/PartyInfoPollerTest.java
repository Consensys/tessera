package com.quorum.tessera.p2p;

import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Party;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class PartyInfoPollerTest {

    private static final String OWN_URL = "http://own.com:8080/";

    private static final String TARGET_URL = "http://bogus.com:9878/";

    private static final String TARGET_URL_2 = "http://otherwebsite.com:9878/";

    private PartyInfoService partyInfoService;

    private PartyInfoPoller partyInfoPoller;

    private NodeInfoPublisher nodeInfoPublisher;

    private Executor executor;

    @Before
    public void setUp() {
        this.partyInfoService = mock(PartyInfoService.class);
        this.nodeInfoPublisher = mock(NodeInfoPublisher.class);
        this.executor = mock(Executor.class);

        doAnswer(
            (InvocationOnMock invocation) -> {
                ((Runnable) invocation.getArguments()[0]).run();
                return null;
            }
        ).when(executor).execute(any(Runnable.class));

        this.partyInfoPoller = new PartyInfoPoller(partyInfoService, nodeInfoPublisher, executor);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(partyInfoService, nodeInfoPublisher);
    }

    @Test
    public void run() {
        final NodeInfo partyInfo = NodeInfo.Builder.create()
            .withUrl(OWN_URL)
            .withParties(Set.of(new Party(TARGET_URL)))
            .build();

        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);
        when(nodeInfoPublisher.publishNodeInfo(TARGET_URL, partyInfo)).thenReturn(true);

        partyInfoPoller.run();

        verify(partyInfoService).getPartyInfo();
        verify(nodeInfoPublisher).publishNodeInfo(TARGET_URL, partyInfo);
    }

    @Test
    public void dontPublishWhenURLIsOwn() {
        final NodeInfo partyInfo = NodeInfo.Builder.create()
            .withUrl(OWN_URL)
            .withParties(Set.of(new Party(OWN_URL)))
            .build();


        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);
        when(nodeInfoPublisher.publishNodeInfo(OWN_URL, partyInfo)).thenReturn(true);

        partyInfoPoller.run();

        verify(partyInfoService).getPartyInfo();
    }

    @Test
    public void exceptionThrowByPostDoesntBubble() {
        final Set<Party> parties = new HashSet<>(Arrays.asList(new Party(TARGET_URL), new Party(TARGET_URL_2)));
        final NodeInfo partyInfo = NodeInfo.Builder.create()
            .withUrl(OWN_URL)
            .withParties(parties)
            .build();

        doReturn(partyInfo).when(partyInfoService).getPartyInfo();
        doThrow(UnsupportedOperationException.class).when(nodeInfoPublisher).publishNodeInfo(TARGET_URL, partyInfo);

        final Throwable throwable = catchThrowable(partyInfoPoller::run);

        assertThat(throwable).isNull();

        verify(nodeInfoPublisher).publishNodeInfo(TARGET_URL, partyInfo);
        verify(nodeInfoPublisher).publishNodeInfo(TARGET_URL_2, partyInfo);
        verify(partyInfoService).getPartyInfo();
    }

    @Test
    public void constructWithMinimalArgs() {
        assertThat(new PartyInfoPoller(partyInfoService, nodeInfoPublisher)).isNotNull();
    }
}
