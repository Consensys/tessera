package com.quorum.tessera.server.monitoring;

public class MBeanResourceMetric implements MBeanMetric {

  private String resourceMethod;

  private String name;

  private String value;

  public MBeanResourceMetric(String resourceMethod, String name, String value) {
    this.resourceMethod = resourceMethod;
    this.name = name;
    this.value = value;
  }

  public String getResourceMethod() {
    return resourceMethod;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }
}
