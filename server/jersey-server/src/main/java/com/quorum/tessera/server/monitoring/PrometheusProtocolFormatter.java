package com.quorum.tessera.server.monitoring;

import com.quorum.tessera.config.AppType;
import java.util.List;

public class PrometheusProtocolFormatter {

  public String format(final List<MBeanMetric> metrics, AppType appType) {
    StringBuilder formattedMetrics = new StringBuilder();

    for (final MBeanMetric metric : metrics) {
      final MBeanResourceMetric resourceMetric = (MBeanResourceMetric) metric;

      formattedMetrics
          .append("tessera_")
          .append(appType)
          .append("_")
          .append(sanitize(resourceMetric.getResourceMethod()))
          .append("_")
          .append(sanitize(resourceMetric.getName()))
          .append(" ")
          .append(resourceMetric.getValue())
          .append("\n");
    }

    return formattedMetrics.toString();
  }

  private String sanitize(final String input) {
    return input
        .replaceAll("(#.*)|(_total)|\\(\\)|\\)|\\[\\]|\\]|;", "")
        .replaceAll("->|\\(|\\[", "_");
  }
}
