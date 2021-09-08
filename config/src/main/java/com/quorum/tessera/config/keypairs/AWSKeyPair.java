package com.quorum.tessera.config.keypairs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.xml.bind.annotation.XmlElement;

public class AWSKeyPair implements ConfigKeyPair {
  @NotNull
  @XmlElement
  @Pattern(
      regexp = "^[0-9a-zA-Z\\-/_+=.@]*$",
      message =
          "AWS Secrets Manager IDs can only contain alphanumeric characters and the characters /_+=.@-")
  private final String publicKeyId;

  @NotNull
  @XmlElement
  @Pattern(
      regexp = "^[0-9a-zA-Z\\-/_+=.@]*$",
      message =
          "AWS Secrets Manager IDs can only contain alphanumeric characters and the characters /_+=.@-")
  private final String privateKeyId;

  public AWSKeyPair(String publicKeyId, String privateKeyId) {
    this.publicKeyId = publicKeyId;
    this.privateKeyId = privateKeyId;
  }

  public String getPublicKeyId() {
    return this.publicKeyId;
  }

  public String getPrivateKeyId() {
    return this.privateKeyId;
  }

  @Override
  public String getPublicKey() {
    // keys are not fetched from Secrets Manager yet so return null
    return null;
  }

  @Override
  public String getPrivateKey() {
    // keys are not fetched from Secrets Manager yet so return null
    return null;
  }

  @Override
  public void withPassword(char[] password) {
    // password not used with Secrets Manager stored keys
  }

  @Override
  public char[] getPassword() {
    // no password to return
    return new char[0];
  }
}
