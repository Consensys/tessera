package com.quorum.tessera.config;

import com.quorum.tessera.config.adapters.PathAdapter;
import com.quorum.tessera.config.constraints.ValidPath;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Path;

@Deprecated
@XmlAccessorType(XmlAccessType.FIELD)
public class HashicorpKeyVaultConfig extends ConfigItem implements KeyVaultConfig {

  @Valid @NotNull @XmlAttribute private String url;

  @Valid @XmlElement private String approlePath;

  @Valid
  @ValidPath(checkExists = true, message = "File does not exist")
  @XmlElement(type = String.class)
  @XmlJavaTypeAdapter(PathAdapter.class)
  private Path tlsKeyStorePath;

  @Valid
  @ValidPath(checkExists = true, message = "File does not exist")
  @XmlElement(type = String.class)
  @XmlJavaTypeAdapter(PathAdapter.class)
  private Path tlsTrustStorePath;

  public HashicorpKeyVaultConfig(
      String url, String approlePath, Path tlsKeyStorePath, Path tlsTrustStorePath) {
    this.url = url;
    this.approlePath = approlePath;
    this.tlsKeyStorePath = tlsKeyStorePath;
    this.tlsTrustStorePath = tlsTrustStorePath;
  }

  public HashicorpKeyVaultConfig() {}

  public String getUrl() {
    return this.url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Path getTlsKeyStorePath() {
    return tlsKeyStorePath;
  }

  public void setTlsKeyStorePath(Path tlsKeyStorePath) {
    this.tlsKeyStorePath = tlsKeyStorePath;
  }

  public Path getTlsTrustStorePath() {
    return tlsTrustStorePath;
  }

  public void setTlsTrustStorePath(Path tlsTrustStorePath) {
    this.tlsTrustStorePath = tlsTrustStorePath;
  }

  public String getApprolePath() {
    if (approlePath == null) {
      return "approle";
    }
    return approlePath;
  }

  public void setApprolePath(String approlePath) {
    this.approlePath = approlePath;
  }

  @Override
  public KeyVaultType getKeyVaultType() {
    return KeyVaultType.HASHICORP;
  }
}
