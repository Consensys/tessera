package com.quorum.tessera.thridparty;

import com.quorum.tessera.service.locator.ServiceLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class ThirdPartyTest {

    private static final String CONTEXT_NAME = "context";

    private ServiceLocator serviceLocator;

    private ThirdPartyRestApp thirdParty;

    @Before
    public void setUp() {
        serviceLocator = mock(ServiceLocator.class);
        thirdParty = new ThirdPartyRestApp(serviceLocator, CONTEXT_NAME);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(serviceLocator);
    }

    @Test
    public void getSingletons() {
        thirdParty.getSingletons();
        verify(serviceLocator).getServices(CONTEXT_NAME);
    }
}
