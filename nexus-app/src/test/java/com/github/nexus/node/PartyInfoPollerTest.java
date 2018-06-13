package com.github.nexus.node;

import com.github.nexus.api.model.ApiPath;
import com.github.nexus.node.model.Party;
import com.github.nexus.node.model.PartyInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

public class PartyInfoPollerTest {

    private PartyInfoService partyInfoService;
    private PartyInfoParser partyInfoParser;

    private ScheduledExecutorService scheduledExecutorService;

    private PartyInfoPoller partyInfoPoller;

    private final long rateInSeconds = 2L;

    private PostDelegate postDelegate;

    public PartyInfoPollerTest() {
    }

    @Before
    public void setUp() {
        postDelegate = mock(PostDelegate.class);
        partyInfoService = mock(PartyInfoService.class);
        partyInfoParser = mock(PartyInfoParser.class);
        scheduledExecutorService = mock(ScheduledExecutorService.class);
        partyInfoPoller = new PartyInfoPoller(partyInfoService, scheduledExecutorService,
            partyInfoParser, postDelegate, rateInSeconds);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(partyInfoService, partyInfoParser, scheduledExecutorService);
    }

    @Test
    public void start() {
        partyInfoPoller.start();
        verify(scheduledExecutorService).scheduleAtFixedRate(partyInfoPoller, rateInSeconds, rateInSeconds, TimeUnit.SECONDS);
        verify(scheduledExecutorService).scheduleAtFixedRate(partyInfoPoller, rateInSeconds, rateInSeconds, TimeUnit.SECONDS);

    }

    @Test
    public void stop() {
        partyInfoPoller.stop();
        verify(scheduledExecutorService).shutdown();
    }

    @Test
    public void run() {

        String url = "http://bogus.com:9878";
        String ownURL = "http://own.com:8080";
        byte[] response = "BOGUS".getBytes();

        when(postDelegate.doPost(url, ApiPath.PARTYINFO, response)).thenReturn(response);

        PartyInfo partyInfo = mock(PartyInfo.class);
        Party party = mock(Party.class);
        when(party.getUrl()).thenReturn(url);
        when(partyInfo.getUrl()).thenReturn(ownURL);
        when(partyInfo.getParties()).thenReturn(singleton(party));

        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);


        when(partyInfoParser.to(partyInfo)).thenReturn("BOGUS".getBytes());

        PartyInfo updatedPartyInfo = mock(PartyInfo.class);
        when(partyInfoParser.from(response)).thenReturn(updatedPartyInfo);

        partyInfoPoller.run();


        verify(partyInfoService).getPartyInfo();
        verify(partyInfoService).updatePartyInfo(updatedPartyInfo);
        verify(partyInfoParser).from(response);
        verify(partyInfoParser).to(partyInfo);
        verify(postDelegate).doPost(url, ApiPath.PARTYINFO,response);

    }

    @Test
    public void testWhenURLISOwn(){
        String ownURL = "http://own.com:8080";
        byte[] response = "BOGUS".getBytes();

        when(postDelegate.doPost(ownURL, ApiPath.PARTYINFO, response)).thenReturn(response);

        PartyInfo partyInfo = mock(PartyInfo.class);
        Party party = mock(Party.class);
        when(party.getUrl()).thenReturn(ownURL);
        when(partyInfo.getUrl()).thenReturn(ownURL);
        when(partyInfo.getParties()).thenReturn(singleton(party));

        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);


        when(partyInfoParser.to(partyInfo)).thenReturn("BOGUS".getBytes());

        PartyInfo updatedPartyInfo = mock(PartyInfo.class);
        when(partyInfoParser.from(response)).thenReturn(updatedPartyInfo);

        partyInfoPoller.run();

        verify(partyInfoParser).to(partyInfo);
        verify(partyInfoService).getPartyInfo();
    }

    @Test
    public void runThrowsException() {

        UnsupportedOperationException someException 
                = new UnsupportedOperationException("OUCH");
        
        when(partyInfoService.getPartyInfo()).thenThrow(someException);

        try {
            partyInfoPoller.run();
            failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
        } catch (UnsupportedOperationException ex) {
            assertThat(ex).isSameAs(someException);
        }
        verify(partyInfoService).getPartyInfo();
    }

}
