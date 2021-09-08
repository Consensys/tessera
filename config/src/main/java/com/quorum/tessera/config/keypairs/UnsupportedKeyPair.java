package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.adapters.PathAdapter;
import com.quorum.tessera.config.constraints.ValidUnsupportedKeyPair;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Path;

@ValidUnsupportedKeyPair
public class UnsupportedKeyPair implements ConfigKeyPair {

  @XmlElement private KeyDataConfig config;

  @XmlElement private String privateKey;

  @XmlElement private String publicKey;

  @XmlElement
  @XmlJavaTypeAdapter(PathAdapter.class)
  private Path privateKeyPath;

  @XmlElement
  @XmlJavaTypeAdapter(PathAdapter.class)
  private Path publicKeyPath;

  @XmlElement private String azureVaultPublicKeyId;

  @XmlElement private String azureVaultPrivateKeyId;

  @XmlElement private String azureVaultPublicKeyVersion;

  @XmlElement private String azureVaultPrivateKeyVersion;

  @XmlElement private String hashicorpVaultPublicKeyId;

  @XmlElement private String hashicorpVaultPrivateKeyId;

  @XmlElement private String hashicorpVaultSecretEngineName;

  @XmlElement private String hashicorpVaultSecretName;

  @XmlElement private String hashicorpVaultSecretVersion;

  @XmlElement private String awsSecretsManagerPublicKeyId;

  @XmlElement private String awsSecretsManagerPrivateKeyId;

  public UnsupportedKeyPair(
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

  public UnsupportedKeyPair() {}

  @Override
  public String getPublicKey() {
    return this.publicKey;
  }

  @Override
  public String getPrivateKey() {
    return this.privateKey;
  }

  public Path getPublicKeyPath() {
    return publicKeyPath;
  }

  public Path getPrivateKeyPath() {
    return privateKeyPath;
  }

  public KeyDataConfig getConfig() {
    return config;
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

  @Override
  public void withPassword(char[] password) {
    // do nothing as password not used with this keypair type
  }

  @Override
  public char[] getPassword() {
    return null;
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

  public String getAwsSecretsManagerPublicKeyId() {
    return awsSecretsManagerPublicKeyId;
  }

  public void setAwsSecretsManagerPublicKeyId(String awsSecretsManagerPublicKeyId) {
    this.awsSecretsManagerPublicKeyId = awsSecretsManagerPublicKeyId;
  }

  public String getAwsSecretsManagerPrivateKeyId() {
    return awsSecretsManagerPrivateKeyId;
  }

  public void setAwsSecretsManagerPrivateKeyId(String awsSecretsManagerPrivateKeyId) {
    this.awsSecretsManagerPrivateKeyId = awsSecretsManagerPrivateKeyId;
  }
}
