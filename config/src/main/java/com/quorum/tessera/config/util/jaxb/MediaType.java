package com.quorum.tessera.config.util.jaxb;

public enum MediaType {
  XML("application/xml"),
  JSON("application/json");

  private final String value;

  MediaType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
