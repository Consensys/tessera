package com.github.tessera.server;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

public class PrometheusResponseFormatterTest {

    private ResponseFormatter responseFormatter;

    @Before
    public void init() {
        this.responseFormatter = new PrometheusResponseFormatter();
    }

    @Test
    public void responseCorrectlyFormatted() {
        HashMap<String, String> mockMetrics = new HashMap<>();
        mockMetrics.put("GET->upCheck()#a10a4f8d_AverageTime[ms]_total", "100");
        mockMetrics.put("POST->resend(ResendRequest)#8ca0a760_RequestRate[requestsPerSeconds]_total", "1.3");

        String expectedResponse = "tessera_GET_upCheck_AverageTime_ms 100" + "\n" +
                                  "tessera_POST_resend_ResendRequest_RequestRate_requestsPerSeconds 1.3";

        assertThat(responseFormatter.createResponse(mockMetrics)).isEqualTo(expectedResponse);
    }

    @Test
    public void noMetricsToFormatIsHandled() {
        HashMap<String, String> emptyMetrics = new HashMap<>();

        String response = responseFormatter.createResponse(emptyMetrics);

        assertThat(responseFormatter.createResponse(emptyMetrics).isEmpty());
    }
}
