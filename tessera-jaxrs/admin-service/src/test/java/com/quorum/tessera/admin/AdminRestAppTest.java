package com.quorum.tessera.admin;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.api.filter.IPWhitelistFilter;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.service.locator.ServiceLocator;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import static org.assertj.core.api.Assertions.assertThat;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import static org.mockito.Mockito.*;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class AdminRestAppTest {

    private MockServiceLocator serviceLocator;

    private Set services;

    private JerseyTest jerseyTest;

    private AdminRestApp adminRestApp;

    @Before
    public void setUp() throws Exception {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        
        this.serviceLocator = (MockServiceLocator) ServiceLocator.create();

        services = new HashSet<>();
        services.add(mock(ConfigService.class));
        services.add(mock(PartyInfoService.class));

        serviceLocator.setServices(services);

        this.adminRestApp = new AdminRestApp();

        this.jerseyTest =
                new JerseyTest() {
                    @Override
                    protected Application configure() {
                        enable(TestProperties.LOG_TRAFFIC);
                        enable(TestProperties.DUMP_ENTITY);
                        ResourceConfig config = ResourceConfig.forApplication(adminRestApp);
                        return config;
                    }
                };

        jerseyTest.setUp();
    }

    @After
    public void tearDown() throws Exception {

        jerseyTest.tearDown();
    }

    @Test
    public void getSingletons() {

        Set<Object> results = this.adminRestApp.getSingletons();

        assertThat(results)
                .hasAtLeastOneElementOfType(IPWhitelistFilter.class)
                .hasAtLeastOneElementOfType(ConfigResource.class)
                .hasSize(2);
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
