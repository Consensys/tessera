package com.quorum.tessera.server.monitoring;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.InfluxConfig;
import com.quorum.tessera.ssl.context.ClientSSLContextFactory;
import com.quorum.tessera.ssl.context.SSLContextFactory;

import javax.management.MBeanServer;
import javax.net.ssl.SSLContext;
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

    private final URI tesseraAppUri;

    private final InfluxConfig influxConfig;

    private final AppType appType;

    private final MBeanServer mbs;

    public InfluxDbClient(URI tesseraAppUri, InfluxConfig influxConfig, AppType appType) {
        this.tesseraAppUri = tesseraAppUri;
        this.influxConfig = influxConfig;
        this.appType = appType;

        this.mbs = ManagementFactory.getPlatformMBeanServer();
    }

    public Response postMetrics() {
        MetricsEnquirer metricsEnquirer = new MetricsEnquirer(mbs);
        List<MBeanMetric> metrics = metricsEnquirer.getMBeanMetrics(appType);

        InfluxDbProtocolFormatter formatter = new InfluxDbProtocolFormatter();
        String formattedMetrics = formatter.format(metrics, tesseraAppUri, appType);

        ClientBuilder clientBuilder = ClientBuilder.newBuilder();

        if (influxConfig.isSsl()) {
            final SSLContextFactory sslContextFactory = ClientSSLContextFactory.create();
            final SSLContext sslContext =
                    sslContextFactory.from(influxConfig.getServerUri().toString(), influxConfig.getSslConfig());

            clientBuilder.sslContext(sslContext);
        }

        Client client = clientBuilder.build();

        WebTarget influxTarget =
                client.target(influxConfig.getServerAddress()).path("write").queryParam("db", influxConfig.getDbName());

        return influxTarget
                .request(MediaType.TEXT_PLAIN)
                .accept(MediaType.TEXT_PLAIN)
                .post(Entity.text(formattedMetrics));
    }
}
