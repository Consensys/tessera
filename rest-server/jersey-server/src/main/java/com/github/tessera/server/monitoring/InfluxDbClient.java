package com.github.tessera.server.monitoring;

import javax.management.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.util.List;

public class InfluxDbClient {
    private final URI uri;
    private final String influxDbName;
    private final String influxHost;
    private final int influxPort;

    private final MBeanServer mbs;

    public InfluxDbClient(URI uri) {
        this.uri = uri;
        this.influxDbName = "tessera_demo";
        this.influxHost = "http://localhost";
        this.influxPort = 8086;

        this.mbs = ManagementFactory.getPlatformMBeanServer();
    }

    public Response postMetrics() {
        MetricsEnquirer metricsEnquirer = new MetricsEnquirer(mbs);
        List<MBeanMetric> metrics = metricsEnquirer.getMBeanMetrics();

        InfluxDbProtocolFormatter formatter = new InfluxDbProtocolFormatter();
        String formattedMetrics = formatter.format(metrics, uri);

        Client client = ClientBuilder.newClient();
        WebTarget influxTarget = client.target(influxHost + ":" + influxPort)
                                       .path("write")
                                       .queryParam("db", influxDbName);

        return influxTarget.request(MediaType.TEXT_PLAIN)
                                        .accept(MediaType.TEXT_PLAIN)
                                        .post(Entity.text(formattedMetrics));
    }
}
