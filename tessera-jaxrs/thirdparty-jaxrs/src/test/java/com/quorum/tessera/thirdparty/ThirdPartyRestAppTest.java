package com.quorum.tessera.thirdparty;

import com.quorum.tessera.config.AppType;
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

@Ignore
public class ThirdPartyRestAppTest {

    private JerseyTest jersey;

    private ThirdPartyRestApp thirdParty;

    @Before
    public void setUp() throws Exception {

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

        assertThat(results).hasSize(3);
    }

    @Test
    public void appType() {
        assertThat(thirdParty.getAppType()).isEqualTo(AppType.THIRD_PARTY);
    }
}
