package com.quorum.tessera.enclave.rest;

import jakarta.xml.bind.annotation.XmlMimeType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class KeyValuePair implements Serializable {

  @XmlMimeType("base64Binary")
  private byte[] key;

  @XmlMimeType("base64Binary")
  private byte[] value;

  public KeyValuePair() {}

  public KeyValuePair(byte[] key, byte[] value) {
    this.key = key;
    this.value = value;
  }

  public byte[] getKey() {
    return key;
  }

  public void setKey(byte[] key) {
    this.key = key;
  }

  public byte[] getValue() {
    return value;
  }

  public void setValue(byte[] value) {
    this.value = value;
  }
}
