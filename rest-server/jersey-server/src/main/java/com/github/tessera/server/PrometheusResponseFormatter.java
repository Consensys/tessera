package com.github.tessera.server;

import java.util.Map;

public class PrometheusResponseFormatter implements ResponseFormatter {

    @Override
    public String createResponse(Map<String, String> metrics) {
        String response = "";

        for(String metricsName : metrics.keySet()) {
            response += "tessera_";
            response += metricsName.replaceAll("->|\\(\\)|[\\[\\]\\(\\)]", "_")
                                   .replaceAll("(#.+?_)|(__total)", "");
            response += " ";
            response += metrics.get(metricsName);
            response += "\n";
        }

        return response.trim();
    }
}
