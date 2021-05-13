package com.quorum.tessera.server.monitoring;

import com.quorum.tessera.config.AppType;
import java.net.URI;
import java.util.List;

public class InfluxDbProtocolFormatter {

  public String format(List<MBeanMetric> metrics, URI uri, AppType appType) {
    StringBuilder formattedMetrics = new StringBuilder();

    for (MBeanMetric metric : metrics) {
      MBeanResourceMetric resourceMetric = (MBeanResourceMetric) metric;

      formattedMetrics
          .append("tessera_")
          .append(appType)
          .append("_")
          .append(sanitize(resourceMetric.getResourceMethod()))
          .append(",")
          .append("instance=")
          .append(uri)
          .append(" ")
          .append(sanitize(resourceMetric.getName()))
          .append("=")
          .append(resourceMetric.getValue())
          .append("\n");
    }

    return formattedMetrics.toString().trim();
  }

  private String sanitize(String input) {
    return input
        .replaceAll("(#.*)|(_total)|\\(\\)|\\)|\\[\\]|\\]|;", "")
        .replaceAll("->|\\(|\\[", "_");
  }
}
