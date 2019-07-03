package com.quorum.tessera.admin;

import com.quorum.tessera.api.filter.IPWhitelistFilter;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.service.locator.ServiceLocator;
import java.util.HashSet;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class AdminRestAppTest {

    private ServiceLocator serviceLocator;

    private AdminRestApp adminRestApp;

    @Before
    public void setUp() {
        this.serviceLocator = mock(ServiceLocator.class);
        this.adminRestApp = new AdminRestApp(serviceLocator);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(serviceLocator);
    }

    @Test
    public void getSingletons() {

        Set services = new HashSet<>();
        services.add(new ConfigResource(mock(ConfigService.class), mock(PartyInfoService.class)));
        services.add(new IPWhitelistFilter(mock(ConfigService.class)));

        when(serviceLocator.getServices()).thenReturn(services);

        Set<Object> results = this.adminRestApp.getSingletons();

        assertThat(results).containsExactlyInAnyOrderElementsOf(services);

        verify(serviceLocator).getServices();
    }

    @Test
    public void getClasses() {

        assertThat(adminRestApp.getClasses()).isNotEmpty();
    }

    @Test
    public void appType() {
        assertThat(adminRestApp.getAppType()).isEqualTo(AppType.ADMIN);
    }
}
