package com.github.tessera.server;

import com.github.tessera.server.MBeanMetric;
import com.github.tessera.server.MBeanResourceMetric;
import com.github.tessera.server.PrometheusResponseFormatter;
import com.github.tessera.server.ResponseFormatter;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;

public class PrometheusResponseFormatterTest {

    private ResponseFormatter responseFormatter;

    @Before
    public void init() {
        this.responseFormatter = new PrometheusResponseFormatter();
    }

    //TODO Split out into separate tests
    @Test
    public void responseCorrectlyFormatted() {
        ArrayList<MBeanMetric> mockMetrics = new ArrayList<>();

        mockMetrics.add(createMBeanResourceMetric("GET->upCheck()#a10a4f8d", "AverageTime[ms]_total", "100" ));
        mockMetrics.add(createMBeanResourceMetric("POST->resend(ResendRequest)#8ca0a760", "RequestRate[requestsPerSeconds]", "1.3" ));
        mockMetrics.add(createMBeanResourceMetric("POST->push(byte[])#7f702b7e", "MinTime[ms]_total", "3.4"));
        mockMetrics.add(createMBeanResourceMetric("GET->receiveRaw(String;String)#fc8f8357", "AverageTime[ms]_total", "5.2"));

        String expectedResponse = "tessera_GET_upCheck_AverageTime_ms 100" + "\n" +
                                  "tessera_POST_resend_ResendRequest_RequestRate_requestsPerSeconds 1.3" + "\n" +
                                  "tessera_POST_push_byte_MinTime_ms 3.4" + "\n" +
                                  "tessera_GET_receiveRaw_StringString_AverageTime_ms 5.2";

        assertThat(responseFormatter.createResponse(mockMetrics)).isEqualTo(expectedResponse);
    }

    private MBeanMetric createMBeanResourceMetric(String resourceMethod, String name, String value) {
        MBeanResourceMetric mBeanResourceMetric = new MBeanResourceMetric();
        mBeanResourceMetric.setResourceMethod(resourceMethod);
        mBeanResourceMetric.setName(name);
        mBeanResourceMetric.setValue(value);

        return mBeanResourceMetric;
    }

    @Test
    public void noMetricsToFormatIsHandled() {
        ArrayList<MBeanMetric> emptyMetrics = new ArrayList<>();

        String response = responseFormatter.createResponse(emptyMetrics);

        assertThat(responseFormatter.createResponse(emptyMetrics).isEmpty());
    }
}
