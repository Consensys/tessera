package com.github.tessera.server.monitoring;

import java.util.List;

public class InfluxProtocolFormatter {

    public String format(List<MBeanMetric> metrics, String uri) {
        String formattedMetrics = "";

        for(MBeanMetric metric : metrics) {
            MBeanResourceMetric resourceMetric = (MBeanResourceMetric) metric;

            formattedMetrics += "tessera_" +
                                sanitize(resourceMetric.getResourceMethod()) +
                                "," +
                                "instance=" + uri +
                                " " +
                                sanitize(resourceMetric.getName()) + "=" + resourceMetric.getValue() +
                                "\n";
        }
        return formattedMetrics.trim();
    }

    private String sanitize(String input) {
        return input.replaceAll("(#.*)|(_total)|\\(\\)|\\)|\\[\\]|\\]|;", "")
            .replaceAll("->|\\(|\\[", "_");
    }
}
