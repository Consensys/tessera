package com.quorum.tessera.q2t;

import com.quorum.tessera.service.locator.ServiceLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class Q2TRestAppTest {

    private static final String CONTEXT_NAME = "context";

    private ServiceLocator serviceLocator;

    private Q2TRestApp q2TRestApp;

    @Before
    public void setUp() {
        serviceLocator = mock(ServiceLocator.class);
        q2TRestApp = new Q2TRestApp(serviceLocator, CONTEXT_NAME);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(serviceLocator);
    }

    @Test
    public void getSingletons() {
        q2TRestApp.getSingletons();
        verify(serviceLocator).getServices(CONTEXT_NAME);
    }
}
