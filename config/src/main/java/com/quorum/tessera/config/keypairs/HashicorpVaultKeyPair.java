package com.quorum.tessera.config.keypairs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.xml.bind.annotation.XmlElement;

public class HashicorpVaultKeyPair implements ConfigKeyPair {

  @NotNull @XmlElement private String publicKeyId;

  @NotNull @XmlElement private String privateKeyId;

  @NotNull @XmlElement private String secretEngineName;

  @NotNull @XmlElement private String secretName;

  @PositiveOrZero(message = "{ValidPositiveInteger.message}")
  @XmlElement
  private Integer secretVersion;

  public HashicorpVaultKeyPair(
      String publicKeyId,
      String privateKeyId,
      String secretEngineName,
      String secretName,
      Integer secretVersion) {
    this.publicKeyId = publicKeyId;
    this.privateKeyId = privateKeyId;
    this.secretEngineName = secretEngineName;
    this.secretName = secretName;
    this.secretVersion = secretVersion;
  }

  public String getPublicKeyId() {
    return publicKeyId;
  }

  public String getPrivateKeyId() {
    return privateKeyId;
  }

  public String getSecretEngineName() {
    return secretEngineName;
  }

  public String getSecretName() {
    return secretName;
  }

  public Integer getSecretVersion() {
    return secretVersion;
  }

  @Override
  public String getPublicKey() {
    // keys are not fetched from vault yet so return null
    return null;
  }

  @Override
  public String getPrivateKey() {
    // keys are not fetched from vault yet so return null
    return null;
  }

  @Override
  public void withPassword(char[] password) {
    // password not used with vault stored keys
  }

  @Override
  public char[] getPassword() {
    // no password to return
    return new char[0];
  }
}
