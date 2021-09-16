package com.quorum.tessera.config;

import com.quorum.tessera.config.constraints.ValidServerAddress;
import com.quorum.tessera.config.constraints.ValidSsl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import java.net.URI;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
public class InfluxConfig extends ConfigItem {

  @ValidServerAddress(
      message = "Server Address is invalid",
      supportedSchemes = {"http", "https"})
  @NotNull
  @XmlElement
  private String serverAddress;

  @NotNull
  @XmlElement(required = true)
  private Long pushIntervalInSecs;

  @NotNull
  @XmlElement(required = true)
  private String dbName;

  @Valid @XmlElement @ValidSsl private SslConfig sslConfig;

  public InfluxConfig() {}

  public boolean isSsl() {
    return Objects.nonNull(sslConfig) && sslConfig.getTls() == SslAuthenticationMode.STRICT;
  }

  public URI getServerUri() {
    try {
      return URI.create(serverAddress);
    } catch (IllegalArgumentException ex) {
      throw new ConfigException(ex);
    }
  }

  public String getServerAddress() {
    return serverAddress;
  }

  public Long getPushIntervalInSecs() {
    return pushIntervalInSecs;
  }

  public String getDbName() {
    return dbName;
  }

  public SslConfig getSslConfig() {
    return sslConfig;
  }

  public void setServerAddress(String serverAddress) {
    this.serverAddress = serverAddress;
  }

  public void setPushIntervalInSecs(Long pushIntervalInSecs) {
    this.pushIntervalInSecs = pushIntervalInSecs;
  }

  public void setDbName(String dbName) {
    this.dbName = dbName;
  }

  public void setSslConfig(SslConfig sslConfig) {
    this.sslConfig = sslConfig;
  }
}
