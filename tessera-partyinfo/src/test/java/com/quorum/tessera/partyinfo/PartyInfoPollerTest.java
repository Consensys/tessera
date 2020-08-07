package com.quorum.tessera.partyinfo;

import com.quorum.tessera.partyinfo.model.NodeInfo;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
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

    private static final byte[] DATA = "BOGUS".getBytes();

    private PartyInfoService partyInfoService;

    private PartyInfoParser partyInfoParser;

    private PartyInfoPoller partyInfoPoller;

    private P2pClient p2pClient;

    private Executor executor;

    @Before
    public void setUp() {
        this.partyInfoService = mock(PartyInfoService.class);
        this.partyInfoParser = mock(PartyInfoParser.class);
        this.p2pClient = mock(P2pClient.class);
        this.executor = mock(Executor.class);

        doAnswer(
            (InvocationOnMock invocation) -> {
                ((Runnable) invocation.getArguments()[0]).run();
                return null;
            }
        ).when(executor).execute(any(Runnable.class));

        when(partyInfoParser.to(any(PartyInfo.class))).thenReturn(DATA);

        this.partyInfoPoller = new PartyInfoPoller(partyInfoService, partyInfoParser, p2pClient, executor);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(partyInfoService, partyInfoParser, p2pClient);
    }

    @Test
    public void run() {
        final NodeInfo partyInfo = NodeInfo.Builder.create()
            .withUrl(OWN_URL)
            .withParties(Set.of(new Party(TARGET_URL)))
            .build();

        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);
        when(p2pClient.sendPartyInfo(TARGET_URL,DATA)).thenReturn(true);

        partyInfoPoller.run();

        verify(partyInfoService).getPartyInfo();
        verify(partyInfoParser).to(any(PartyInfo.class));
        verify(p2pClient).sendPartyInfo(TARGET_URL, DATA);
    }

    @Test
    public void testWhenURLIsOwn() {
        final NodeInfo partyInfo = NodeInfo.Builder.create()
            .withUrl(OWN_URL)
            .withParties(Set.of(new Party(OWN_URL)))
            .build();


        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);
        when(partyInfoParser.to(any(PartyInfo.class))).thenReturn(DATA);
        when(p2pClient.sendPartyInfo(OWN_URL,DATA)).thenReturn(true);

        partyInfoPoller.run();

        verify(partyInfoParser).to(any(PartyInfo.class));
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
        doThrow(UnsupportedOperationException.class).when(p2pClient).sendPartyInfo(TARGET_URL, DATA);

        final Throwable throwable = catchThrowable(partyInfoPoller::run);

        assertThat(throwable).isNull();

        verify(p2pClient).sendPartyInfo(TARGET_URL, DATA);
        verify(p2pClient).sendPartyInfo(TARGET_URL_2, DATA);
        verify(partyInfoService).getPartyInfo();
        verify(partyInfoParser).to(any(PartyInfo.class));
    }

    @Test
    public void constructWithMinimalArgs() {
        assertThat(new PartyInfoPoller(partyInfoService, p2pClient)).isNotNull();
    }
}
