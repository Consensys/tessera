package com.quorum.tessera.server.monitoring;

import java.util.List;

public class PrometheusProtocolFormatter {

    public String format(final List<MBeanMetric> metrics) {
        StringBuilder formattedMetrics = new StringBuilder();

        for (final MBeanMetric metric : metrics) {
            final MBeanResourceMetric resourceMetric = (MBeanResourceMetric) metric;

            formattedMetrics.append("tessera_")
                .append(sanitize(resourceMetric.getResourceMethod()))
                .append("_")
                .append(sanitize(resourceMetric.getName()))
                .append(" ")
                .append(resourceMetric.getValue())
                .append("\n");
        }

        return formattedMetrics.toString().trim();
    }

    private String sanitize(final String input) {
        return input
            .replaceAll("(#.*)|(_total)|\\(\\)|\\)|\\[\\]|\\]|;", "")
            .replaceAll("->|\\(|\\[", "_");
    }

}
