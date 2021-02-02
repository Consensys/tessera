package com.quorum.tessera.q2t;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.service.locator.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Application;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class Q2TRestAppTest {

    private JerseyTest jersey;

    private Q2TRestApp q2TRestApp;

    @Before
    public void setUp() throws Exception {
        final Set<Object> services = Set.of(mock(Config.class));

        final MockServiceLocator serviceLocator = (MockServiceLocator) ServiceLocator.create();
        serviceLocator.setServices(services);

        q2TRestApp = new Q2TRestApp();

        jersey =
                new JerseyTest() {
                    @Override
                    protected Application configure() {
                        enable(TestProperties.LOG_TRAFFIC);
                        enable(TestProperties.DUMP_ENTITY);
                        return ResourceConfig.forApplication(q2TRestApp);
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
        assertThat(q2TRestApp.getSingletons()).hasSize(6);
    }

    @Test
    public void appType() {
        assertThat(q2TRestApp.getAppType()).isEqualTo(AppType.Q2T);
    }
}
