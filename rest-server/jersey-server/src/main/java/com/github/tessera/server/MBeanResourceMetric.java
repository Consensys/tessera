package com.github.tessera.server;

public class MBeanResourceMetric implements MBeanMetric {
    private String resourceMethod;
    private String name;
    private String value;

    public String getResourceMethod() {
        return resourceMethod;
    }

    public void setResourceMethod(String resourceMethod) {
        this.resourceMethod = resourceMethod;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value.toString();
    }

}
