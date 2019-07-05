package com.quorum.tessera.q2t;

import com.quorum.tessera.api.filter.IPWhitelistFilter;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.service.locator.ServiceLocator;
import java.util.HashSet;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.*;

@Ignore
public class Q2TRestAppTest {

    private ServiceLocator serviceLocator;

    private Q2TRestApp q2TRestApp;

    @Before
    public void setUp() {
        serviceLocator = mock(ServiceLocator.class);
        q2TRestApp = new Q2TRestApp(serviceLocator);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(serviceLocator);
    }

    @Test
    public void getSingletons() {

        Set services = new HashSet();
        services.add(mock(IPWhitelistFilter.class));
        services.add(mock(TransactionResource.class));

        when(serviceLocator.getServices()).thenReturn(services);

        Set<Object> results = q2TRestApp.getSingletons();

        assertThat(results).containsExactlyElementsOf(services);

        verify(serviceLocator).getServices();
    }

    @Test
    public void appType() {
        assertThat(q2TRestApp.getAppType()).isEqualTo(AppType.Q2T);
    }
}
