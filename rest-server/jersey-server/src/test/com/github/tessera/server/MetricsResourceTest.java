package com.github.tessera.server;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class MetricsResourceTest extends JerseyTest {

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

    @Test
    public void validResponseReturnsMetrics() {
        final Response testResponse = target("/metrics").request().get(Response.class);

        HashMap<String, String> metrics = new HashMap<>();
        metrics.put("GET->upCheck()#a10a4f8d_AverageTime[ms]_total", "100");
        metrics.put("POST->resend(ResendRequest)#8ca0a760_RequestRate[requestsPerSeconds]_total", "1.3");

        String formattedResponse = "tessera_GET_upCheck_AverageTime_ms 100" + "\n" +
                                   "tessera_POST_resend_ResendRequest_RequestRate_requestsPerSeconds 1.3";

        ResponseFormatter responseFormatter = mock(PrometheusResponseFormatter.class);
        when(responseFormatter.createResponse(any(Map.class))).thenReturn(formattedResponse);


    }

}

