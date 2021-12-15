package com.quorum.tessera.server.monitoring;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.InfluxConfig;
import com.quorum.tessera.ssl.context.ClientSSLContextFactory;
import com.quorum.tessera.ssl.context.SSLContextFactory;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.util.List;
import javax.management.MBeanServer;
import javax.net.ssl.SSLContext;

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
          sslContextFactory.from(
              influxConfig.getServerUri().toString(), influxConfig.getSslConfig());

      clientBuilder.sslContext(sslContext);
    }

    Client client = clientBuilder.build();

    WebTarget influxTarget =
        client
            .target(influxConfig.getServerUri())
            .path("write")
            .queryParam("db", influxConfig.getDbName());

    return influxTarget
        .request(MediaType.TEXT_PLAIN)
        .accept(MediaType.TEXT_PLAIN)
        .post(Entity.text(formattedMetrics));
  }
}
