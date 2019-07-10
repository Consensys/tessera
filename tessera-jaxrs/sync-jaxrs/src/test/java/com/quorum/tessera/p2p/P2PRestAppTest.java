package com.quorum.tessera.p2p;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.admin.ConfigService;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.service.locator.ServiceLocator;
import com.quorum.tessera.transaction.TransactionManager;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.core.Application;
import static org.assertj.core.api.Assertions.assertThat;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class P2PRestAppTest {

    private P2PRestApp p2PRestApp;

    private JerseyTest jersey;

    @Before
    public void setUp() throws Exception {

        Set services = new HashSet<>();
        services.add(mock(PartyInfoService.class));
        services.add(mock(ConfigService.class));
        services.add(mock(TransactionManager.class));
        services.add(mock(Enclave.class));

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setApp(AppType.P2P);
        serverConfig.setServerAddress("http://localhost:9928");
        serverConfig.setEnabled(true);

        final Config config = new Config();
        config.setServerConfigs(Collections.singletonList(serverConfig));

        services.add(config);

        MockServiceLocator serviceLocator = (MockServiceLocator) ServiceLocator.create();
        serviceLocator.setServices(services);

        p2PRestApp = new P2PRestApp();

        jersey =
                new JerseyTest() {
                    @Override
                    protected Application configure() {
                        enable(TestProperties.LOG_TRAFFIC);
                        enable(TestProperties.DUMP_ENTITY);
                        ResourceConfig jerseyconfig = ResourceConfig.forApplication(p2PRestApp);
                        return jerseyconfig;
                    }
                };

        jersey.setUp();
    }

    @After
    public void tearDown() throws Exception {
        jersey.tearDown();
    }

    @Test
    public void getSingletons() {
        Set<Object> results = p2PRestApp.getSingletons();
        assertThat(results).hasSize(3);
    }

    @Test
    public void appType() {
        assertThat(p2PRestApp.getAppType()).isEqualTo(AppType.P2P);
    }

    @Test(expected = IllegalStateException.class)
    public void noEnclave() {
        ServiceLocator serviceLocator = mock(ServiceLocator.class);

        Set services =
                (Set) createServices().stream().filter(o -> !Enclave.class.isInstance(o)).collect(Collectors.toSet());

        when(serviceLocator.getServices()).thenReturn(services);

        new P2PRestApp(serviceLocator);
    }

    @Test(expected = IllegalStateException.class)
    public void noPartyInfoService() {
        ServiceLocator serviceLocator = mock(ServiceLocator.class);

        Set services =
                (Set)
                        createServices().stream()
                                .filter(o -> !PartyInfoService.class.isInstance(o))
                                .collect(Collectors.toSet());

        when(serviceLocator.getServices()).thenReturn(services);

        new P2PRestApp(serviceLocator);
    }

    @Test(expected = IllegalStateException.class)
    public void noConfig() {
        ServiceLocator serviceLocator = mock(ServiceLocator.class);

        Set services =
                (Set) createServices().stream().filter(o -> !Config.class.isInstance(o)).collect(Collectors.toSet());

        when(serviceLocator.getServices()).thenReturn(services);

        new P2PRestApp(serviceLocator);
    }

    private Set createServices() {
        Set services = new HashSet<>();
        services.add(mock(PartyInfoService.class));
        services.add(mock(ConfigService.class));
        services.add(mock(TransactionManager.class));
        services.add(mock(Enclave.class));

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setApp(AppType.P2P);
        serverConfig.setServerAddress("http://localhost:9928");
        serverConfig.setEnabled(true);

        final Config config = new Config();
        config.setServerConfigs(Collections.singletonList(serverConfig));

        services.add(config);

        return services;
    }
}
