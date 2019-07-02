package com.quorum.tessera.app;

import com.quorum.tessera.admin.AdminRestApp;
import com.quorum.tessera.p2p.P2PRestApp;
import com.quorum.tessera.q2t.Q2TRestApp;
import com.quorum.tessera.service.locator.ServiceLocator;
import com.quorum.tessera.thridparty.ThirdPartyRestApp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import static org.mockito.Mockito.*;

public class RestAppTest {

    private ServiceLocator serviceLocator;

    private P2PRestApp p2PRestApp;

    @Before
    public void setUp() {
        serviceLocator = mock(ServiceLocator.class);
        p2PRestApp = new P2PRestApp(serviceLocator);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(serviceLocator);
    }

    @Test
    public void getSingletons() {
        p2PRestApp.getSingletons();
        verify(serviceLocator).getServices();
    }

    @Test
    public void createWithNoServiceLocator() {

        final Throwable throwable = catchThrowable(() -> new P2PRestApp(null));
        assertThat(throwable).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void constructWithEmptyConstructor() throws Exception {

        Class[] clazzes = new Class[] {P2PRestApp.class, Q2TRestApp.class, ThirdPartyRestApp.class, AdminRestApp.class};

        for (Class c : clazzes) {
            assertThat(c.getDeclaredConstructor().newInstance()).describedAs(c.toString()).isNotNull();
        }
    }
}
