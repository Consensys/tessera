package com.quorum.tessera.config.util;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SomeObject {

  private String someValue;

  public String getSomeValue() {
    return someValue;
  }

  public void setSomeValue(final String someValue) {
    this.someValue = someValue;
  }
}
