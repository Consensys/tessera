package com.quorum.tessera.partyinfo;

import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.ConnectException;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class PartyInfoPollerTest {

    private static final String OWN_URL = "http://own.com:8080/";

    private static final String TARGET_URL = "http://bogus.com:9878/";

    private static final byte[] DATA = "BOGUS".getBytes();

    private PartyInfoService partyInfoService;

    private PartyInfoParser partyInfoParser;

    private PartyInfoPoller partyInfoPoller;

    private P2pClient p2pClient;

    @Before
    public void setUp() {
        this.partyInfoService = mock(PartyInfoService.class);
        this.partyInfoParser = mock(PartyInfoParser.class);
        this.p2pClient = mock(P2pClient.class);
        this.partyInfoPoller = new PartyInfoPoller(partyInfoService, partyInfoParser, p2pClient);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(partyInfoService, partyInfoParser, p2pClient);
    }

    @Test
    public void run() {

        doReturn(true).when(p2pClient).sendPartyInfo(TARGET_URL, DATA);

        final PartyInfo partyInfo = new PartyInfo(OWN_URL, emptySet(), singleton(new Party(TARGET_URL)));
        doReturn(partyInfo).when(partyInfoService).getPartyInfo();

        doReturn(DATA).when(partyInfoParser).to(partyInfo);

        final PartyInfo updatedPartyInfo = new PartyInfo(OWN_URL, emptySet(), singleton(new Party(TARGET_URL)));
        doReturn(updatedPartyInfo).when(partyInfoParser).from(DATA);

        partyInfoPoller.run();

        verify(partyInfoService).getPartyInfo();

        verify(partyInfoParser).to(partyInfo);

        verify(p2pClient).sendPartyInfo(TARGET_URL, DATA);
    }

    @Test
    public void testWhenURLIsOwn() {

        doReturn(true).when(p2pClient).sendPartyInfo(OWN_URL, DATA);

        final PartyInfo partyInfo = new PartyInfo(OWN_URL, emptySet(), singleton(new Party(OWN_URL)));
        doReturn(partyInfo).when(partyInfoService).getPartyInfo();
        doReturn(DATA).when(partyInfoParser).to(partyInfo);

        final PartyInfo updatedPartyInfo = mock(PartyInfo.class);
        doReturn(updatedPartyInfo).when(partyInfoParser).from(DATA);

        partyInfoPoller.run();

        verify(partyInfoParser).to(partyInfo);
        verify(partyInfoService).getPartyInfo();
    }

    @Test
    public void testWhenPostFails() {

        doReturn(false).when(p2pClient).sendPartyInfo(TARGET_URL, DATA);

        final PartyInfo partyInfo = new PartyInfo(OWN_URL, emptySet(), singleton(new Party(TARGET_URL)));

        doReturn(partyInfo).when(partyInfoService).getPartyInfo();
        doReturn(DATA).when(partyInfoParser).to(partyInfo);

        final PartyInfo updatedPartyInfo = mock(PartyInfo.class);
        doReturn(updatedPartyInfo).when(partyInfoParser).from(DATA);

        partyInfoPoller.run();

        verify(partyInfoParser, never()).from(DATA);
        verify(partyInfoParser).to(partyInfo);
        verify(partyInfoService).getPartyInfo();
        verify(p2pClient).sendPartyInfo(TARGET_URL, DATA);
    }

    @Test
    public void runThrowsException() {

        final PartyInfo partyInfo = new PartyInfo(OWN_URL, emptySet(), singleton(new Party(TARGET_URL)));

        doReturn(partyInfo).when(partyInfoService).getPartyInfo();
        doReturn(DATA).when(partyInfoParser).to(partyInfo);

        PartyInfo updatedPartyInfo = mock(PartyInfo.class);
        doReturn(updatedPartyInfo).when(partyInfoParser).from(DATA);

        doThrow(UnsupportedOperationException.class).when(p2pClient).sendPartyInfo(TARGET_URL, DATA);

        final Throwable throwable = catchThrowable(partyInfoPoller::run);
        assertThat(throwable).isInstanceOf(UnsupportedOperationException.class);

        verify(p2pClient).sendPartyInfo(TARGET_URL, DATA);

        verify(partyInfoService).getPartyInfo();
        verify(partyInfoService, never()).updatePartyInfo(updatedPartyInfo);
        verify(partyInfoParser, never()).from(DATA);
        verify(partyInfoParser).to(partyInfo);
    }

    @Test
    public void runThrowsConnectionExceptionAndDoesNotThrow() {

        final PartyInfo partyInfo = new PartyInfo(OWN_URL, emptySet(), singleton(new Party(TARGET_URL)));

        doReturn(partyInfo).when(partyInfoService).getPartyInfo();
        doReturn(DATA).when(partyInfoParser).to(partyInfo);

        final PartyInfo updatedPartyInfo = mock(PartyInfo.class);
        doReturn(updatedPartyInfo).when(partyInfoParser).from(DATA);

        final RuntimeException connectionException = new RuntimeException(new ConnectException("OUCH"));
        doThrow(connectionException).when(p2pClient).sendPartyInfo(TARGET_URL, DATA);

        partyInfoPoller.run();

        verify(p2pClient).sendPartyInfo(TARGET_URL, DATA);

        verify(partyInfoService).getPartyInfo();
        verify(partyInfoParser, never()).from(DATA);
        verify(partyInfoParser).to(partyInfo);
    }

    @Test
    public void constructWithMinimalArgs() {
        assertThat(new PartyInfoPoller(partyInfoService, p2pClient)).isNotNull();
    }

    @Test
    public void noP2pClient() throws Exception {

        PartyInfoPoller partyInfoPollerWithNullClient = new PartyInfoPoller(partyInfoService, partyInfoParser, null);
        partyInfoPollerWithNullClient.run();
    }
}
