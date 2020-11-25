package net.consensys.tessera.b2t;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
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

public class B2TRestAppTest {

    private JerseyTest jersey;

    private MockServiceLocator serviceLocator;

    private B2TRestApp app;

    @Before
    public void setUp() throws Exception {
        final Set<Object> services = Set.of(mock(Config.class));

        serviceLocator = (MockServiceLocator) ServiceLocator.create();
        serviceLocator.setServices(services);

        app = new B2TRestApp();

        jersey = new JerseyTest() {
            @Override
            protected Application configure() {
                enable(TestProperties.LOG_TRAFFIC);
                enable(TestProperties.DUMP_ENTITY);
                return ResourceConfig.forApplication(app);
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
        assertThat(app.getSingletons()).hasSize(1);
    }

}
