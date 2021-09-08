package com.quorum.tessera.server.jersey;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SamplePayload {

  private String id;

  private String value;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
