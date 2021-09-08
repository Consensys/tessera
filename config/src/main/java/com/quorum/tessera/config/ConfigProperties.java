package com.quorum.tessera.config;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class ConfigProperties {

  @XmlAnyElement private List<JAXBElement<String>> properties = new ArrayList<>();

  public ConfigProperties() {}

  public List<JAXBElement<String>> getProperties() {
    return properties;
  }

  public void setProperties(List<JAXBElement<String>> properties) {
    this.properties = properties;
  }
}
