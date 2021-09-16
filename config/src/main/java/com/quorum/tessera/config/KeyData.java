package com.quorum.tessera.config;

import com.quorum.tessera.config.adapters.PathAdapter;
import jakarta.validation.constraints.Pattern;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Path;

@XmlAccessorType(XmlAccessType.FIELD)
public class KeyData extends ConfigItem {

  @XmlElement private char[] password;

  @XmlElement private KeyDataConfig config;

  @XmlElement private String privateKey;

  @XmlElement private String publicKey;

  @XmlElement
  @XmlJavaTypeAdapter(PathAdapter.class)
  private Path privateKeyPath;

  @XmlElement
  @XmlJavaTypeAdapter(PathAdapter.class)
  private Path publicKeyPath;

  @XmlElement
  @Pattern(regexp = "^[0-9a-zA-Z\\-]*$")
  private String azureVaultPublicKeyId;

  @XmlElement
  @Pattern(regexp = "^[0-9a-zA-Z\\-]*$")
  private String azureVaultPrivateKeyId;

  @XmlElement private String azureVaultPublicKeyVersion;

  @XmlElement private String azureVaultPrivateKeyVersion;

  @XmlElement private String hashicorpVaultPublicKeyId;

  @XmlElement private String hashicorpVaultPrivateKeyId;

  @XmlElement private String hashicorpVaultSecretEngineName;

  @XmlElement private String hashicorpVaultSecretName;

  @XmlElement private String hashicorpVaultSecretVersion;

  @XmlElement private String awsSecretsManagerPublicKeyId;

  @XmlElement private String awsSecretsManagerPrivateKeyId;

  public KeyData(
      KeyDataConfig config,
      String privateKey,
      String publicKey,
      Path privateKeyPath,
      Path publicKeyPath,
      String azureVaultPublicKeyId,
      String azureVaultPrivateKeyId,
      String azureVaultPublicKeyVersion,
      String azureVaultPrivateKeyVersion,
      String hashicorpVaultPublicKeyId,
      String hashicorpVaultPrivateKeyId,
      String hashicorpVaultSecretEngineName,
      String hashicorpVaultSecretName,
      String hashicorpVaultSecretVersion,
      String awsSecretsManagerPublicKeyId,
      String awsSecretsManagerPrivateKeyId) {
    this.config = config;
    this.privateKey = privateKey;
    this.publicKey = publicKey;
    this.privateKeyPath = privateKeyPath;
    this.publicKeyPath = publicKeyPath;
    this.azureVaultPublicKeyId = azureVaultPublicKeyId;
    this.azureVaultPrivateKeyId = azureVaultPrivateKeyId;
    this.azureVaultPublicKeyVersion = azureVaultPublicKeyVersion;
    this.azureVaultPrivateKeyVersion = azureVaultPrivateKeyVersion;
    this.hashicorpVaultPublicKeyId = hashicorpVaultPublicKeyId;
    this.hashicorpVaultPrivateKeyId = hashicorpVaultPrivateKeyId;
    this.hashicorpVaultSecretEngineName = hashicorpVaultSecretEngineName;
    this.hashicorpVaultSecretName = hashicorpVaultSecretName;
    this.hashicorpVaultSecretVersion = hashicorpVaultSecretVersion;
    this.awsSecretsManagerPublicKeyId = awsSecretsManagerPublicKeyId;
    this.awsSecretsManagerPrivateKeyId = awsSecretsManagerPrivateKeyId;
  }

  public KeyData() {}

  public String getPrivateKey() {
    return privateKey;
  }

  public String getPublicKey() {
    return publicKey;
  }

  public KeyDataConfig getConfig() {
    return config;
  }

  public Path getPrivateKeyPath() {
    return privateKeyPath;
  }

  public Path getPublicKeyPath() {
    return publicKeyPath;
  }

  public String getAzureVaultPublicKeyId() {
    return azureVaultPublicKeyId;
  }

  public String getAzureVaultPrivateKeyId() {
    return azureVaultPrivateKeyId;
  }

  public String getAzureVaultPublicKeyVersion() {
    return azureVaultPublicKeyVersion;
  }

  public String getAzureVaultPrivateKeyVersion() {
    return azureVaultPrivateKeyVersion;
  }

  public String getHashicorpVaultPublicKeyId() {
    return hashicorpVaultPublicKeyId;
  }

  public String getHashicorpVaultPrivateKeyId() {
    return hashicorpVaultPrivateKeyId;
  }

  public String getHashicorpVaultSecretEngineName() {
    return hashicorpVaultSecretEngineName;
  }

  public String getHashicorpVaultSecretName() {
    return hashicorpVaultSecretName;
  }

  public String getHashicorpVaultSecretVersion() {
    return hashicorpVaultSecretVersion;
  }

  public String getAwsSecretsManagerPublicKeyId() {
    return awsSecretsManagerPublicKeyId;
  }

  public String getAwsSecretsManagerPrivateKeyId() {
    return awsSecretsManagerPrivateKeyId;
  }

  public void setConfig(KeyDataConfig config) {
    this.config = config;
  }

  public void setPrivateKey(String privateKey) {
    this.privateKey = privateKey;
  }

  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }

  public void setPrivateKeyPath(Path privateKeyPath) {
    this.privateKeyPath = privateKeyPath;
  }

  public void setPublicKeyPath(Path publicKeyPath) {
    this.publicKeyPath = publicKeyPath;
  }

  public void setAzureVaultPublicKeyId(String azureVaultPublicKeyId) {
    this.azureVaultPublicKeyId = azureVaultPublicKeyId;
  }

  public void setAzureVaultPrivateKeyId(String azureVaultPrivateKeyId) {
    this.azureVaultPrivateKeyId = azureVaultPrivateKeyId;
  }

  public void setAzureVaultPublicKeyVersion(String azureVaultPublicKeyVersion) {
    this.azureVaultPublicKeyVersion = azureVaultPublicKeyVersion;
  }

  public void setAzureVaultPrivateKeyVersion(String azureVaultPrivateKeyVersion) {
    this.azureVaultPrivateKeyVersion = azureVaultPrivateKeyVersion;
  }

  public void setHashicorpVaultPublicKeyId(String hashicorpVaultPublicKeyId) {
    this.hashicorpVaultPublicKeyId = hashicorpVaultPublicKeyId;
  }

  public void setHashicorpVaultPrivateKeyId(String hashicorpVaultPrivateKeyId) {
    this.hashicorpVaultPrivateKeyId = hashicorpVaultPrivateKeyId;
  }

  public void setHashicorpVaultSecretEngineName(String hashicorpVaultSecretEngineName) {
    this.hashicorpVaultSecretEngineName = hashicorpVaultSecretEngineName;
  }

  public void setHashicorpVaultSecretName(String hashicorpVaultSecretName) {
    this.hashicorpVaultSecretName = hashicorpVaultSecretName;
  }

  public void setHashicorpVaultSecretVersion(String hashicorpVaultSecretVersion) {
    this.hashicorpVaultSecretVersion = hashicorpVaultSecretVersion;
  }

  public void setAwsSecretsManagerPublicKeyId(String awsSecretsManagerPublicKeyId) {
    this.awsSecretsManagerPublicKeyId = awsSecretsManagerPublicKeyId;
  }

  public void setAwsSecretsManagerPrivateKeyId(String awsSecretsManagerPrivateKeyId) {
    this.awsSecretsManagerPrivateKeyId = awsSecretsManagerPrivateKeyId;
  }

  public char[] getPassword() {
    return password;
  }

  public void setPassword(char[] password) {
    this.password = password;
  }
}
