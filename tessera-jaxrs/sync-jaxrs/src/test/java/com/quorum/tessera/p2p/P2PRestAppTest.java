package com.quorum.tessera.p2p;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.api.common.UpCheckResource;
import com.quorum.tessera.api.filter.IPWhitelistFilter;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.context.RuntimeContextFactory;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.service.locator.ServiceLocator;
import com.quorum.tessera.transaction.TransactionManager;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Application;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class P2PRestAppTest {

    private P2PRestApp p2PRestApp;

    private JerseyTest jersey;

    static final RuntimeContext runtimeContext = RuntimeContextFactory.newFactory().create(mock(Config.class));

    @Before
    public void setUp() throws Exception {

        Set services = new HashSet<>();
        services.add(mock(TransactionManager.class));
        services.add(mock(Enclave.class));

        Client client = mock(Client.class);
        when(runtimeContext.getP2pClient()).thenReturn(client);
        when(runtimeContext.isRemoteKeyValidation()).thenReturn(true);
        when(runtimeContext.getP2pServerUri()).thenReturn(URI.create("http://own.com/"));
        when(runtimeContext.getPeers()).thenReturn(List.of(URI.create("http://peer.com/")));

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
        assertThat(results).hasSize(5);
        results.forEach(
                o ->
                        assertThat(o)
                                .isInstanceOfAny(
                                        PartyInfoResource.class,
                                        IPWhitelistFilter.class,
                                        UpCheckResource.class,
                                        TransactionResource.class,
                                        PrivacyGroupResource.class));
    }

    @Test
    public void recoverP2PApp() {
        when(runtimeContext.isRecoveryMode()).thenReturn(true);
        p2PRestApp = new P2PRestApp();
        Set<Object> results = p2PRestApp.getSingletons();
        assertThat(results).hasSize(4);
        results.forEach(
                o ->
                        assertThat(o)
                                .isInstanceOfAny(
                                        PartyInfoResource.class,
                                        IPWhitelistFilter.class,
                                        UpCheckResource.class,
                                        RecoveryResource.class));
    }

    @Test
    public void appType() {
        assertThat(p2PRestApp.getAppType()).isEqualTo(AppType.P2P);
    }
}
