package com.quorum.tessera.p2p;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.context.RuntimeContextFactory;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.service.locator.ServiceLocator;
import com.quorum.tessera.transaction.resend.batch.BatchResendManager;
import com.quorum.tessera.transaction.TransactionManager;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.client.Client;
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

    static final RuntimeContext runtimeContext = RuntimeContextFactory.newFactory().create(mock(Config.class));

    @Before
    public void setUp() throws Exception {

        Set services = new HashSet<>();
        services.add(mock(PartyInfoService.class));
        services.add(mock(TransactionManager.class));
        services.add(mock(Enclave.class));
        services.add(mock(BatchResendManager.class));

        Client client = mock(Client.class);
        when(runtimeContext.getP2pClient()).thenReturn(client);
        when(runtimeContext.isRemoteKeyValidation()).thenReturn(true);

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
}
