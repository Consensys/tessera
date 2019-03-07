package com.quorum.tessera.admin;

import com.quorum.tessera.service.locator.ServiceLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class AdminRestAppTest {

    private static final String CONTEXT_NAME = "context";

    private ServiceLocator serviceLocator;

    private AdminRestApp adminRestApp;

    @Before
    public void setUp() {
        this.serviceLocator = mock(ServiceLocator.class);

        this.adminRestApp = new AdminRestApp(serviceLocator, CONTEXT_NAME);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(serviceLocator);
    }

    @Test
    public void getSingletons() {
        this.adminRestApp.getSingletons();

        verify(serviceLocator).getServices(CONTEXT_NAME);
    }

}

