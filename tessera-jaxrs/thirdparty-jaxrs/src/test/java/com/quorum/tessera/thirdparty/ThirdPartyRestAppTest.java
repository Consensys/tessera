package com.quorum.tessera.thirdparty;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.api.filter.IPWhitelistFilter;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.service.locator.ServiceLocator;
import com.quorum.tessera.transaction.TransactionManager;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import static org.assertj.core.api.Assertions.assertThat;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class ThirdPartyRestAppTest {

    private JerseyTest jersey;

    private MockServiceLocator serviceLocator;

    private ThirdPartyRestApp thirdParty;

    @Before
    public void setUp() throws Exception {
        serviceLocator = (MockServiceLocator) ServiceLocator.create();

        Set services = new HashSet();
        services.add(mock(IPWhitelistFilter.class));
        services.add(mock(TransactionManager.class));
        services.add(mock(PartyInfoService.class));

        serviceLocator.setServices(services);

        thirdParty = new ThirdPartyRestApp();

        jersey =
                new JerseyTest() {
                    @Override
                    protected Application configure() {
                        enable(TestProperties.LOG_TRAFFIC);
                        enable(TestProperties.DUMP_ENTITY);
                        ResourceConfig jerseyconfig = ResourceConfig.forApplication(thirdParty);
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

        Set<Object> results = thirdParty.getSingletons();

        assertThat(results).hasSize(4);
    }

    @Test
    public void appType() {
        assertThat(thirdParty.getAppType()).isEqualTo(AppType.THIRD_PARTY);
    }
}
