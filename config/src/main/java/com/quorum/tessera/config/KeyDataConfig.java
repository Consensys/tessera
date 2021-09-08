package com.quorum.tessera.config;

import com.quorum.tessera.config.adapters.PrivateKeyTypeAdapter;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Optional;

@XmlAccessorType(XmlAccessType.FIELD)
public class KeyDataConfig {

  @NotNull
  @XmlElement(name = "data")
  private PrivateKeyData privateKeyData;

  @NotNull
  @XmlAttribute
  @XmlJavaTypeAdapter(PrivateKeyTypeAdapter.class)
  private PrivateKeyType type;

  public KeyDataConfig(PrivateKeyData privateKeyData, PrivateKeyType type) {
    this.privateKeyData = privateKeyData;
    this.type = type;
  }

  public KeyDataConfig() {}

  public PrivateKeyType getType() {
    return type;
  }

  public PrivateKeyData getPrivateKeyData() {
    return privateKeyData;
  }

  public String getValue() {
    return Optional.ofNullable(privateKeyData).map(PrivateKeyData::getValue).orElse(null);
  }

  public String getSnonce() {
    return Optional.ofNullable(privateKeyData).map(PrivateKeyData::getSnonce).orElse(null);
  }

  public String getAsalt() {
    return Optional.ofNullable(privateKeyData).map(PrivateKeyData::getAsalt).orElse(null);
  }

  public String getSbox() {
    return Optional.ofNullable(privateKeyData).map(PrivateKeyData::getSbox).orElse(null);
  }

  public ArgonOptions getArgonOptions() {
    return Optional.ofNullable(privateKeyData).map(PrivateKeyData::getArgonOptions).orElse(null);
  }
}
