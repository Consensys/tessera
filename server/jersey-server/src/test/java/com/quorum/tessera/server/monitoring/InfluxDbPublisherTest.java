package com.quorum.tessera.server.monitoring;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class InfluxDbPublisherTest {

    private InfluxDbPublisher influxDbPublisher;

    private InfluxDbClient influxDbClient;

    @Before
    public void setUp() {
        this.influxDbClient = mock(InfluxDbClient.class);

        this.influxDbPublisher = new InfluxDbPublisher(influxDbClient);
    }

    @Test
    public void runMethodIsCalled() {
        influxDbPublisher.run();
        verify(influxDbClient).postMetrics();
    }

}
