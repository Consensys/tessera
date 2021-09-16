package com.quorum.tessera.config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
public class CrossDomainConfig extends ConfigItem {

  @XmlElement private List<String> allowedMethods;

  @XmlElement private List<String> allowedOrigins;

  @XmlElement private List<String> allowedHeaders;

  @XmlElement private Boolean allowCredentials;

  public List<String> getAllowedOrigins() {
    return allowedOrigins;
  }

  public void setAllowedOrigins(List<String> allowedOrigins) {
    this.allowedOrigins = allowedOrigins;
  }

  public List<String> getAllowedMethods() {
    return Objects.requireNonNullElse(
        allowedMethods, Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
  }

  public void setAllowedMethods(List<String> allowedMethods) {
    this.allowedMethods = allowedMethods;
  }

  public List<String> getAllowedHeaders() {
    return allowedHeaders;
  }

  public void setAllowedHeaders(List<String> allowedHeaders) {
    this.allowedHeaders = allowedHeaders;
  }

  public Boolean getAllowCredentials() {
    return Objects.requireNonNullElse(allowCredentials, Boolean.TRUE);
  }

  public void setAllowCredentials(Boolean allowCredentials) {
    this.allowCredentials = allowCredentials;
  }
}
