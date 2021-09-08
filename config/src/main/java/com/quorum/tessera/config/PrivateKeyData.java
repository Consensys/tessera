package com.quorum.tessera.config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class PrivateKeyData extends ConfigItem {

  @XmlElement(name = "bytes")
  private String value;

  @XmlElement private String snonce;

  @XmlElement private String asalt;

  @XmlElement private String sbox;

  @XmlElement(name = "aopts")
  private ArgonOptions argonOptions;

  public PrivateKeyData(
      String value, String snonce, String asalt, String sbox, ArgonOptions argonOptions) {
    this.value = value;
    this.snonce = snonce;
    this.asalt = asalt;
    this.sbox = sbox;
    this.argonOptions = argonOptions;
  }

  public PrivateKeyData() {}

  public String getValue() {
    return value;
  }

  public String getSnonce() {
    return snonce;
  }

  public String getAsalt() {
    return asalt;
  }

  public String getSbox() {
    return sbox;
  }

  public ArgonOptions getArgonOptions() {
    return argonOptions;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setSnonce(String snonce) {
    this.snonce = snonce;
  }

  public void setAsalt(String asalt) {
    this.asalt = asalt;
  }

  public void setSbox(String sbox) {
    this.sbox = sbox;
  }

  public void setArgonOptions(ArgonOptions argonOptions) {
    this.argonOptions = argonOptions;
  }
}
