package com.quorum.tessera.server.monitoring;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import com.quorum.tessera.config.AppType;
import java.lang.management.ManagementFactory;
import java.util.List;
import javax.management.MBeanServer;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

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
    final StringBuilder formattedMetrics = new StringBuilder();

    // TODO Each app server has a /metrics endpoint but currently each endpoint returns the metrics
    // for all servers.  Would be better to lock this down e.g. <p2puri>/metrics only returns the
    // p2p metrics
    for (AppType type : AppType.values()) {
      List<MBeanMetric> metrics = metricsEnquirer.getMBeanMetrics(type);
      PrometheusProtocolFormatter formatter = new PrometheusProtocolFormatter();

      formattedMetrics.append(formatter.format(metrics, type));
    }

    return Response.status(Response.Status.OK)
        .header(HttpHeaders.CONTENT_TYPE, TEXT_PLAIN)
        .entity(formattedMetrics.toString())
        .build();
  }
}
