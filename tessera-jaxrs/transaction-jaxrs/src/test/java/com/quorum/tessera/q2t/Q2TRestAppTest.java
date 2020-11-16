package com.quorum.tessera.q2t;


import com.quorum.tessera.config.AppType;
import com.quorum.tessera.transaction.EncodedPayloadManager;
import com.quorum.tessera.transaction.TransactionManager;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Application;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

public class Q2TRestAppTest {

    private JerseyTest jersey;

    private Q2TRestApp q2TRestApp;

    @Before
    public void setUpAndGetSingletons() throws Exception {

        q2TRestApp = new Q2TRestApp();
        try (
            var mockedStaticPayloadManager = mockStatic(EncodedPayloadManager.class);
            var mockedStaticTransactionManager = mockStatic(TransactionManager.class);
        ) {
            mockedStaticTransactionManager.when(TransactionManager::create).thenReturn(mock(TransactionManager.class));

            EncodedPayloadManager encodedPayloadManager = mock(EncodedPayloadManager.class);
            mockedStaticPayloadManager.when(EncodedPayloadManager::getInstance)
                .thenReturn(Optional.of(encodedPayloadManager));


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

            Set<Object> results = q2TRestApp.getSingletons();

            assertThat(results).hasSize(3);
        }
    }

    @After
    public void tearDown() throws Exception {
        jersey.tearDown();
    }



    @Test
    public void appType() {
        assertThat(q2TRestApp.getAppType()).isEqualTo(AppType.Q2T);
    }
}
