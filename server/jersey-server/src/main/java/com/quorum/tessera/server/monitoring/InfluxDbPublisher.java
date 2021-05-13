package com.quorum.tessera.server.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfluxDbPublisher implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(InfluxDbPublisher.class);

  private final InfluxDbClient client;

  public InfluxDbPublisher(InfluxDbClient client) {
    this.client = client;
  }

  @Override
  public void run() {
    LOGGER.info("InfluxDbPublisher executed...");
    client.postMetrics();
  }
}
