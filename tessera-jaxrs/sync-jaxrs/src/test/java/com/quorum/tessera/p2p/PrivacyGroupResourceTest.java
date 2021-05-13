package com.quorum.tessera.p2p;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class PrivacyGroupResourceTest {

  private JerseyTest jersey;

  private PrivacyGroupManager privacyGroupManager;

  @BeforeClass
  public static void setUpLoggers() {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }

  @Before
  public void onSetup() throws Exception {

    privacyGroupManager = mock(PrivacyGroupManager.class);
    PrivacyGroupResource resource = new PrivacyGroupResource(privacyGroupManager);

    jersey =
        new JerseyTest() {
          @Override
          protected Application configure() {
            forceSet(TestProperties.CONTAINER_PORT, "0");
            enable(TestProperties.LOG_TRAFFIC);
            enable(TestProperties.DUMP_ENTITY);
            return new ResourceConfig().register(resource);
          }
        };

    jersey.setUp();
  }

  @After
  public void onTearDown() throws Exception {
    verifyNoMoreInteractions(privacyGroupManager);
    jersey.tearDown();
  }

  @Test
  public void testStorePrivacyGroup() {
    doNothing().when(privacyGroupManager).storePrivacyGroup("encoded".getBytes());

    final Response response =
        jersey
            .target("pushPrivacyGroup")
            .request()
            .post(Entity.entity("encoded".getBytes(), MediaType.APPLICATION_OCTET_STREAM));

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(200);

    verify(privacyGroupManager).storePrivacyGroup("encoded".getBytes());
  }
}
