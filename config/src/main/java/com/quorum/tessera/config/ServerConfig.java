package com.quorum.tessera.config;

import com.quorum.tessera.config.adapters.MapAdapter;
import com.quorum.tessera.config.constraints.ValidServerAddress;
import com.quorum.tessera.config.constraints.ValidSsl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
public class ServerConfig extends ConfigItem {

  @NotNull
  @XmlElement(required = true)
  private AppType app;

  @XmlElement private CommunicationType communicationType = CommunicationType.REST;

  @Valid @XmlElement @ValidSsl private SslConfig sslConfig;

  @Valid @XmlElement private InfluxConfig influxConfig;

  @ValidServerAddress(
      message = "Binding Address is invalid",
      isBindingAddress = true,
      supportedSchemes = {"http", "https"})
  @XmlElement
  private String bindingAddress;

  @ValidServerAddress(message = "Server Address is invalid")
  @NotNull
  @XmlElement
  private String serverAddress;

  @XmlElement(name = "cors")
  private CrossDomainConfig crossDomainConfig;

  @XmlJavaTypeAdapter(MapAdapter.class)
  @XmlElement
  private Map<String, String> properties = Collections.emptyMap();

  /** @deprecated USe default constructor and setters */
  @Deprecated
  public ServerConfig(
      final AppType app,
      final String serverAddress,
      final CommunicationType communicationType,
      final SslConfig sslConfig,
      final InfluxConfig influxConfig,
      final String bindingAddress) {
    this.app = app;
    this.serverAddress = serverAddress;
    this.communicationType = communicationType;
    this.sslConfig = sslConfig;
    this.influxConfig = influxConfig;
    this.bindingAddress = bindingAddress;
  }

  public ServerConfig() {}

  public String getBindingAddress() {
    return this.bindingAddress == null ? this.serverAddress : this.bindingAddress;
  }

  public URI getServerUri() {
    try {
      return URI.create(serverAddress);
    } catch (IllegalArgumentException ex) {
      throw new ConfigException(ex);
    }
  }

  public boolean isSsl() {
    return Objects.nonNull(sslConfig) && sslConfig.getTls() == SslAuthenticationMode.STRICT;
  }

  public URI getBindingUri() {
    try {
      return new URI(this.getBindingAddress());
    } catch (URISyntaxException ex) {
      throw new ConfigException(ex);
    }
  }

  public AppType getApp() {
    return app;
  }

  public void setApp(AppType app) {
    this.app = app;
  }

  public CommunicationType getCommunicationType() {
    return communicationType;
  }

  public void setCommunicationType(CommunicationType communicationType) {
    this.communicationType = communicationType;
  }

  public SslConfig getSslConfig() {
    return sslConfig;
  }

  public void setSslConfig(SslConfig sslConfig) {
    this.sslConfig = sslConfig;
  }

  public InfluxConfig getInfluxConfig() {
    return influxConfig;
  }

  public void setInfluxConfig(InfluxConfig influxConfig) {
    this.influxConfig = influxConfig;
  }

  public void setBindingAddress(String bindingAddress) {
    this.bindingAddress = bindingAddress;
  }

  public String getServerAddress() {
    return serverAddress;
  }

  public void setServerAddress(String serverAddress) {
    this.serverAddress = serverAddress;
  }

  public boolean isUnixSocket() {
    return Objects.equals(getServerUri().getScheme(), "unix");
  }

  public CrossDomainConfig getCrossDomainConfig() {
    return crossDomainConfig;
  }

  public void setCrossDomainConfig(CrossDomainConfig crossDomainConfig) {
    this.crossDomainConfig = crossDomainConfig;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }
}
