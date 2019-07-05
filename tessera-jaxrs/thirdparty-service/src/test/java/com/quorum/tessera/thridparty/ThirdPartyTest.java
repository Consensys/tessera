package com.quorum.tessera.thridparty;

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
public class ThirdPartyTest {

    private ServiceLocator serviceLocator;

    private ThirdPartyRestApp thirdParty;

    @Before
    public void setUp() {
        serviceLocator = mock(ServiceLocator.class);
        thirdParty = new ThirdPartyRestApp(serviceLocator);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(serviceLocator);
    }

    @Test
    public void getSingletons() {
        Set services = new HashSet();
        services.add(mock(IPWhitelistFilter.class));
        services.add(mock(RawTransactionResource.class));

        when(serviceLocator.getServices()).thenReturn(services);

        Set<Object> results = thirdParty.getSingletons();

        assertThat(results).containsExactlyElementsOf(services);

        verify(serviceLocator).getServices();
    }

    @Test
    public void appType() {
        assertThat(thirdParty.getAppType()).isEqualTo(AppType.THIRD_PARTY);
    }
}
