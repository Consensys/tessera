package com.quorum.tessera.server.monitoring;

import javax.management.*;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.lang.management.ManagementFactory;
import java.util.List;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

@Path("/metrics")
public class MetricsResource {

    private final MBeanServer mbs;

    public MetricsResource() {
        mbs = ManagementFactory.getPlatformMBeanServer();
    }

    @GET
    @Produces("text/plain")
    public Response getMetrics() {
        MetricsEnquirer metricsEnquirer = new MetricsEnquirer(mbs);
        List<MBeanMetric> metrics = metricsEnquirer.getMBeanMetrics();

        PrometheusProtocolFormatter formatter = new PrometheusProtocolFormatter();
        String formattedMetrics = formatter.format(metrics);

        return Response.status(Response.Status.OK)
            .header("Content-Type", TEXT_PLAIN)
            .entity(formattedMetrics)
            .build();
    }
}
