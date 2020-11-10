package com.quorum.tessera.q2t;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.transaction.EncodedPayloadManager;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.Application;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@Ignore
public class Q2TRestAppTest {

    private JerseyTest jersey;

    private Q2TRestApp q2TRestApp;

    @Before
    public void setUp() throws Exception {

        Config config = mock(Config.class);
        EncodedPayloadManager.create(config);

        q2TRestApp = new Q2TRestApp();

        jersey =
                new JerseyTest() {
                    @Override
                    protected Application configure() {
                        enable(TestProperties.LOG_TRAFFIC);
                        enable(TestProperties.DUMP_ENTITY);
                        ResourceConfig jerseyconfig = ResourceConfig.forApplication(q2TRestApp);
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

        Set<Object> results = q2TRestApp.getSingletons();

        assertThat(results).hasSize(3);
    }

    @Test
    public void appType() {
        assertThat(q2TRestApp.getAppType()).isEqualTo(AppType.Q2T);
    }
}
