package com.github.tessera.server;

import java.util.List;

public interface ResponseFormatter {
    String createResponse(List<MBeanMetric> metrics);
}
