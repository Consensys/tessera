package com.github.tessera.server;

import java.util.List;

public class PrometheusResponseFormatter implements ResponseFormatter {

    @Override
    public String createResponse(List<MBeanMetric> metrics) {
        String response = "";

        //TODO https://www.javaworld.com/article/2461744/design-patterns/java-language-iterating-over-collections-in-java-8.html
        for(MBeanMetric metric : metrics) {
            MBeanResourceMetric resourceMetric = (MBeanResourceMetric) metric;

            response += "tessera_" +
                        sanitize(resourceMetric.getResourceMethod()) +
                        "_" +
                        sanitize(resourceMetric.getName()) +
                        " " +
                        resourceMetric.getValue() +
                        "\n";
//            response += resourceMetric.getResourceMethod() + " " + resourceMetric.getName() + " " + resourceMetric.getValue() + "\n";
        }
        return response.trim();
    }

    private String sanitize(String input) {
        return input.replaceAll("(#.*)|(_total)|\\(\\)|\\)|\\[\\]|\\]|;", "")
                    .replaceAll("->|\\(|\\[", "_");
    }


}
