package com.github.tessera.server.monitoring;

import java.util.List;

public interface ProtocolFormatter {
    String format(List<MBeanMetric> metrics);
}
