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
    private final int port;
    private final String dbName;
    private final String hostName;

    private final MBeanServer mbs;

    public InfluxDbClient(URI uri, int port, String hostName, String dbName) {
        this.uri = uri;
        this.port = port;
        this.hostName = hostName;
        this.dbName = dbName;

        this.mbs = ManagementFactory.getPlatformMBeanServer();
    }

    public Response postMetrics() {
        MetricsEnquirer metricsEnquirer = new MetricsEnquirer(mbs);
        List<MBeanMetric> metrics = metricsEnquirer.getMBeanMetrics();

        InfluxDbProtocolFormatter formatter = new InfluxDbProtocolFormatter();
        String formattedMetrics = formatter.format(metrics, uri);

        Client client = ClientBuilder.newClient();
        WebTarget influxTarget = client.target(hostName + ":" + port)
                                       .path("write")
                                       .queryParam("db", dbName);

        return influxTarget.request(MediaType.TEXT_PLAIN)
                                        .accept(MediaType.TEXT_PLAIN)
                                        .post(Entity.text(formattedMetrics));
    }
}
