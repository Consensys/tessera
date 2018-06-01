
package com.github.nexus.api;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import org.mockito.MockitoAnnotations;


public class UpCheckResourceTest  extends JerseyTest {
    
    public UpCheckResourceTest() {
    }
    
    @Override
    public Application configure() {
        MockitoAnnotations.initMocks(this);
        return new ResourceConfig()
                .register(new UpCheckResource());
    }

    @Test
    public void upcheck() {

        Response response = target("/upcheck")
                .request(MediaType.TEXT_PLAIN)
                .get();

        assertThat(response.readEntity(String.class))
                .isEqualTo("I'm up!");
        
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
