package com.github.tessera.server.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;

public class InfluxPublisher implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxPublisher.class);

    private final InfluxDbClient client;

    public InfluxPublisher(InfluxDbClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        LOGGER.info("InfluxPublisher executed...");
        try {
            client.addMetrics();
        } catch (MalformedObjectNameException | IntrospectionException | MBeanException | AttributeNotFoundException | ReflectionException | InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
