package com.github.tessera.server.monitoring;

import java.util.List;

public class PrometheusProtocolFormatter {

    public String format(List<MBeanMetric> metrics) {
        StringBuilder formattedMetrics = new StringBuilder();

        //TODO https://www.javaworld.com/article/2461744/design-patterns/java-language-iterating-over-collections-in-java-8.html
        for(MBeanMetric metric : metrics) {
            MBeanResourceMetric resourceMetric = (MBeanResourceMetric) metric;

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

    private String sanitize(String input) {
        return input.replaceAll("(#.*)|(_total)|\\(\\)|\\)|\\[\\]|\\]|;", "")
                    .replaceAll("->|\\(|\\[", "_");
    }


}
