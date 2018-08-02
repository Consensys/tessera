package com.quorum.tessera.server.monitoring;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class PrometheusProtocolFormatterTest {

    private PrometheusProtocolFormatter protocolFormatter;

    private List<MBeanMetric> mockMetrics;

    @Before
    public void setUp() {
        this.protocolFormatter = new PrometheusProtocolFormatter();
        this.mockMetrics = new ArrayList<>();
    }

    @Test
    public void noArgResourceResponseCorrectlyFormatted() {
        mockMetrics.add(new MBeanResourceMetric("GET->upCheck()#a10a4f8d", "AverageTime[ms]_total", "100"));

        String expectedResponse = "tessera_GET_upCheck_AverageTime_ms 100";

        assertThat(protocolFormatter.format(mockMetrics)).isEqualTo(expectedResponse);
    }

    @Test
    public void singleArgResourceResponseCorrectlyFormatted() {
        mockMetrics.add(new MBeanResourceMetric("POST->resend(ResendRequest)#8ca0a760", "RequestRate[requestsPerSeconds]", "1.3"));

        String expectedResponse = "tessera_POST_resend_ResendRequest_RequestRate_requestsPerSeconds 1.3";

        assertThat(protocolFormatter.format(mockMetrics)).isEqualTo(expectedResponse);
    }

    @Test
    public void singleArrayArgResourceResponseCorrectlyFormatted() {
        mockMetrics.add(new MBeanResourceMetric("POST->push(byte[])#7f702b7e", "MinTime[ms]_total", "3.4"));

        String expectedResponse = "tessera_POST_push_byte_MinTime_ms 3.4";

        assertThat(protocolFormatter.format(mockMetrics)).isEqualTo(expectedResponse);
    }

    @Test
    public void multipleArgResourceResponseCorrectlyFormatted() {
        mockMetrics.add(new MBeanResourceMetric("GET->receiveRaw(String;String)#fc8f8357", "AverageTime[ms]_total", "5.2"));

        String expectedResponse = "tessera_GET_receiveRaw_StringString_AverageTime_ms 5.2";

        assertThat(protocolFormatter.format(mockMetrics)).isEqualTo(expectedResponse);
    }

    @Test
    public void multipleMetricsResponseCorrectlyFormatted() {
        mockMetrics.add(new MBeanResourceMetric("GET->upCheck()#a10a4f8d", "AverageTime[ms]_total", "100"));
        mockMetrics.add(new MBeanResourceMetric("POST->resend(ResendRequest)#8ca0a760", "RequestRate[requestsPerSeconds]", "1.3"));

        String expectedResponse = "tessera_GET_upCheck_AverageTime_ms 100" + "\n" +
            "tessera_POST_resend_ResendRequest_RequestRate_requestsPerSeconds 1.3";

        assertThat(protocolFormatter.format(mockMetrics)).isEqualTo(expectedResponse);
    }

    @Test
    public void noMetricsToFormatIsHandled() {
        assertThat(protocolFormatter.format(mockMetrics)).isEmpty();
    }
}
