package com.github.tessera.server;

public interface MBeanMetric {
    String getName();
    void setName(String name);
    String getValue();
    void setValue(Object value);

}
