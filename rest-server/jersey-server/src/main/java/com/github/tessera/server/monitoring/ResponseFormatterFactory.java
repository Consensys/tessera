package com.github.tessera.server.monitoring;

public class ResponseFormatterFactory {
    public ProtocolFormatter getResponseFormatter() {
            return new PrometheusProtocolFormatter();
    }
}
