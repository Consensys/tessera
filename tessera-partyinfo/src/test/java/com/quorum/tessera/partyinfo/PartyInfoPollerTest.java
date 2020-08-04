package com.quorum.tessera.partyinfo;

import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
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
            (invocation) -> {
                invocation.getArgument(0,Runnable.class).run();
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
        final PartyInfo partyInfo = new PartyInfo(OWN_URL, emptySet(), singleton(new Party(TARGET_URL)));
        doReturn(partyInfo).when(partyInfoService).getPartyInfo();
        doReturn(true).when(p2pClient).sendPartyInfo(TARGET_URL, DATA);

        partyInfoPoller.run();

        verify(partyInfoService).getPartyInfo();
        verify(partyInfoParser).to(partyInfo);
        verify(p2pClient).sendPartyInfo(TARGET_URL, DATA);
    }

    @Test
    public void testWhenURLIsOwn() {
        final PartyInfo partyInfo = new PartyInfo(OWN_URL, emptySet(), singleton(new Party(OWN_URL)));
        doReturn(partyInfo).when(partyInfoService).getPartyInfo();
        doReturn(DATA).when(partyInfoParser).to(partyInfo);
        doReturn(true).when(p2pClient).sendPartyInfo(OWN_URL, DATA);

        partyInfoPoller.run();

        verify(partyInfoParser).to(partyInfo);
        verify(partyInfoService).getPartyInfo();
    }

    @Test
    public void exceptionThrowByPostDoesntBubble() {
        final Set<Party> parties = Stream.of(TARGET_URL,TARGET_URL_2)
            .map(Party::new)
            .collect(Collectors.toUnmodifiableSet());

        final PartyInfo partyInfo = new PartyInfo(OWN_URL, emptySet(), parties);
        doReturn(partyInfo).when(partyInfoService).getPartyInfo();
        doThrow(UnsupportedOperationException.class).when(p2pClient).sendPartyInfo(TARGET_URL, DATA);

        final Throwable throwable = catchThrowable(partyInfoPoller::run);

        assertThat(throwable).isNull();

        verify(p2pClient).sendPartyInfo(TARGET_URL, DATA);
        verify(p2pClient).sendPartyInfo(TARGET_URL_2, DATA);
        verify(partyInfoService).getPartyInfo();
        verify(partyInfoParser).to(partyInfo);
        verify(partyInfoService).removeRecipient(TARGET_URL);
    }

    @Test
    public void constructWithMinimalArgs() {
        assertThat(new PartyInfoPoller(partyInfoService, p2pClient)).isNotNull();
    }
}
