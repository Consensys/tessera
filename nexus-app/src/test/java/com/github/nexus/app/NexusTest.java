package com.github.nexus.app;

import com.github.nexus.service.locator.ServiceLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class NexusTest {

    private ServiceLocator serviceLocator;

    private Nexus nexus;

    public NexusTest() {
    }

    @Before
    public void setUp() {
        serviceLocator = mock(ServiceLocator.class);
        nexus = new Nexus(serviceLocator);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(serviceLocator);
    }

    @Test
    public void getSingletons() {
        nexus.getSingletons();
        verify(serviceLocator).getServices();
    }

    @Test(expected = NullPointerException.class)
    public void createWithNoServiceLocator() {
        new Nexus(null);
    }
}
