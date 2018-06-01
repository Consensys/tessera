package com.github.nexus.api;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

public class VersionResourceTest extends JerseyTest {

    public VersionResourceTest() {
    }

    @Override
    public Application configure() {
        MockitoAnnotations.initMocks(this);
        return new ResourceConfig()
                .register(new VersionResource());
    }

    @Test
    public void getVersion() {

        Response response = target("/version")
                .request(MediaType.TEXT_PLAIN)
                .get();

        assertThat(response.readEntity(String.class))
                .isEqualTo("No version defined yet!");
        
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
