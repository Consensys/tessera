package com.quorum.tessera.server.monitoring;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class InfluxDbPublisherTest {

    private InfluxDbPublisher influxDbPublisher;

    @Mock
    InfluxDbClient influxDbClient;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.influxDbPublisher = new InfluxDbPublisher(influxDbClient);
    }

    @Test
    public void runMethodIsCalled() {
        influxDbPublisher.run();
        verify(influxDbClient, times(1)).postMetrics();
    }

}
