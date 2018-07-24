package com.github.tessera.server.monitoring;

import java.net.URI;
import java.util.List;

public class InfluxProtocolFormatter {

    public String format(List<MBeanMetric> metrics, URI uri) {
        String formattedMetrics = "";

        for(MBeanMetric metric : metrics) {
            MBeanResourceMetric resourceMetric = (MBeanResourceMetric) metric;

            formattedMetrics += "tessera_" +
                                sanitize(resourceMetric.getResourceMethod()) +
                                "," +
                                "instance=" + uri.getHost() + ":" + uri.getPort() +
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
