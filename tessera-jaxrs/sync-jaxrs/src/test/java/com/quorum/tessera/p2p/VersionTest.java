package com.quorum.tessera.p2p;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.jaxrs.client.VersionHeaderDecorator;
import com.quorum.tessera.shared.Constants;
import java.util.Objects;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class VersionTest {

  private JerseyTest jersey;

  @Before
  public void setUp() throws Exception {

    jersey =
        new JerseyTest() {
          @Override
          protected Application configure() {
            forceSet(TestProperties.CONTAINER_PORT, "0");
            enable(TestProperties.LOG_TRAFFIC);
            enable(TestProperties.DUMP_ENTITY);
            return new ResourceConfig().register(new MockResource());
          }
        };

    jersey.setUp();
  }

  @After
  public void tearDown() throws Exception {
    jersey.tearDown();
  }

  @Test
  public void testVersionInClientRequestNotNull() {
    Response response =
        jersey.target("test").register(VersionHeaderDecorator.class).request().get();
    assertThat(response.readEntity(boolean.class)).isTrue();
  }

  @Path("/")
  public class MockResource {

    @Path("test")
    @GET
    public boolean test(@HeaderParam(Constants.API_VERSION_HEADER) final String version) {
      return Objects.nonNull(version);
    }
  }
}
