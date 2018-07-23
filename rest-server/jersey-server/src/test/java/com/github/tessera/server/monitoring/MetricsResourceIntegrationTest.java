package com.github.tessera.server.monitoring;

import com.github.tessera.server.monitoring.MetricsResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

public class MetricsResourceIntegrationTest extends JerseyTest {
    @Override
    protected Application configure() {
        return new ResourceConfig(MetricsResource.class);
    }

    @Test
    public void responseHasCorrectHeaders() {
        final Response testResponse = target("/metrics").request().get(Response.class);

        assertThat(testResponse.getStatus()).isEqualTo(OK.getStatusCode());
        assertThat(testResponse.getHeaderString("Content-Type")).isEqualTo(TEXT_PLAIN);
    }
}
