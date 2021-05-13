package com.quorum.tessera.server.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.AppType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;

public class InfluxDbProtocolFormatterTest {
  private InfluxDbProtocolFormatter protocolFormatter;

  private ArrayList<MBeanMetric> mockMetrics;

  URI mockUri;

  AppType mockAppType;

  @Before
  public void setUp() throws URISyntaxException {
    this.protocolFormatter = new InfluxDbProtocolFormatter();
    this.mockMetrics = new ArrayList<>();
    this.mockUri = new URI("http://localhost:8080");
    this.mockAppType = AppType.P2P;
  }

  @Test
  public void noArgResourceResponseCorrectlyFormatted() {
    mockMetrics.add(
        new MBeanResourceMetric("GET->upCheck()#a10a4f8d", "AverageTime[ms]_total", "100"));

    String expectedResponse =
        "tessera_P2P_GET_upCheck,instance=http://localhost:8080 AverageTime_ms=100";

    assertThat(protocolFormatter.format(mockMetrics, mockUri, mockAppType))
        .isEqualTo(expectedResponse);
  }

  @Test
  public void singleArgResourceResponseCorrectlyFormatted() {
    mockMetrics.add(
        new MBeanResourceMetric(
            "POST->resend(ResendRequest)#8ca0a760", "RequestRate[requestsPerSeconds]", "1.3"));

    String expectedResponse =
        "tessera_P2P_POST_resend_ResendRequest,instance=http://localhost:8080 RequestRate_requestsPerSeconds=1.3";

    assertThat(protocolFormatter.format(mockMetrics, mockUri, mockAppType))
        .isEqualTo(expectedResponse);
  }

  @Test
  public void singleArrayArgResourceResponseCorrectlyFormatted() {
    mockMetrics.add(
        new MBeanResourceMetric("POST->push(byte[])#7f702b7e", "MinTime[ms]_total", "3.4"));

    String expectedResponse =
        "tessera_P2P_POST_push_byte,instance=http://localhost:8080 MinTime_ms=3.4";

    assertThat(protocolFormatter.format(mockMetrics, mockUri, mockAppType))
        .isEqualTo(expectedResponse);
  }

  @Test
  public void multipleArgResourceResponseCorrectlyFormatted() {
    mockMetrics.add(
        new MBeanResourceMetric(
            "GET->receiveRaw(String;String)#fc8f8357", "AverageTime[ms]_total", "5.2"));

    String expectedResponse =
        "tessera_P2P_GET_receiveRaw_StringString,instance=http://localhost:8080 AverageTime_ms=5.2";

    assertThat(protocolFormatter.format(mockMetrics, mockUri, mockAppType))
        .isEqualTo(expectedResponse);
  }

  @Test
  public void multipleMetricsResponseCorrectlyFormatted() {
    mockMetrics.add(
        new MBeanResourceMetric("GET->upCheck()#a10a4f8d", "AverageTime[ms]_total", "100"));
    mockMetrics.add(
        new MBeanResourceMetric(
            "POST->resend(ResendRequest)#8ca0a760", "RequestRate[requestsPerSeconds]", "1.3"));

    String expectedResponse =
        "tessera_P2P_GET_upCheck,instance=http://localhost:8080 AverageTime_ms=100"
            + "\n"
            + "tessera_P2P_POST_resend_ResendRequest,instance=http://localhost:8080 RequestRate_requestsPerSeconds=1.3";

    assertThat(protocolFormatter.format(mockMetrics, mockUri, mockAppType))
        .isEqualTo(expectedResponse);
  }

  @Test
  public void noMetricsToFormatIsHandled() {
    assertThat(protocolFormatter.format(mockMetrics, mockUri, mockAppType)).isEmpty();
  }
}
