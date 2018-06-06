package com.github.nexus;

import com.github.nexus.service.PartyInfoService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class PartyInfoPollerTest {

    private PartyInfoService partyInfoService;

    private ScheduledExecutorService scheduledExecutorService;

    private PartyInfoPoller partyInfoPoller;

    private final long rateInSeconds = 2L;

    public PartyInfoPollerTest() {
    }

    @Before
    public void setUp() {
        partyInfoService = mock(PartyInfoService.class);
        scheduledExecutorService = mock(ScheduledExecutorService.class);
        partyInfoPoller = new PartyInfoPoller(partyInfoService, scheduledExecutorService, rateInSeconds);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(partyInfoService, scheduledExecutorService);

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
        partyInfoPoller.run();
        verify(partyInfoService).pollPartyInfo();

    }

    @Test
    public void runThrowsException() {

        UnsupportedOperationException someException 
                = new UnsupportedOperationException("OUCH");
        
        when(partyInfoService.pollPartyInfo()).thenThrow(someException);
        
        try {
            partyInfoPoller.run();
            failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
        } catch (UnsupportedOperationException ex) {
            assertThat(ex).isSameAs(someException);
        }
        verify(partyInfoService).pollPartyInfo();
    }

}
