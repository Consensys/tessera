package com.github.tessera.server.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;

public class InfluxDbPublisher implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxDbPublisher.class);

    private final InfluxDbClient client;

    public InfluxDbPublisher(InfluxDbClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        LOGGER.info("InfluxDbPublisher executed...");
        try {
            client.postMetrics();
        } catch (MalformedObjectNameException | IntrospectionException | MBeanException | AttributeNotFoundException | ReflectionException | InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
