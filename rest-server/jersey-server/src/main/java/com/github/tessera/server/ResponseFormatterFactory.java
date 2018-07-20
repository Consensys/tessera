package com.github.tessera.server;

public class ResponseFormatterFactory {
    public ResponseFormatter getResponseFormatter() {
            return new PrometheusResponseFormatter();
    }
}
