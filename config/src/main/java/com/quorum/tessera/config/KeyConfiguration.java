package com.quorum.tessera.config;

import com.quorum.tessera.config.adapters.PathAdapter;
import com.quorum.tessera.config.constraints.ValidPath;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@XmlAccessorType(XmlAccessType.FIELD)
public class KeyConfiguration extends ConfigItem {

  @ValidPath(checkExists = true, message = "Password file does not exist")
  @XmlElement(type = String.class)
  @XmlJavaTypeAdapter(PathAdapter.class)
  private Path passwordFile;

  @Size(
      max = 0,
      message =
          "For security reasons, passwords should not be provided directly in the config.  Provide them in a separate file with \"passwordFile\" or at the CLI prompt during node startup.")
  private List<String> passwords;

  @Valid
  @NotNull
  @Size(min = 1, message = "At least 1 public/private key pair must be provided")
  private List<KeyData> keyData;

  @XmlElement private List<@Valid DefaultKeyVaultConfig> keyVaultConfigs;

  @Valid @XmlElement private AzureKeyVaultConfig azureKeyVaultConfig;

  @Valid @XmlElement private HashicorpKeyVaultConfig hashicorpKeyVaultConfig;

  public KeyConfiguration(
      final Path passwordFile,
      final List<String> passwords,
      final List<KeyData> keyData,
      final AzureKeyVaultConfig azureKeyVaultConfig,
      final HashicorpKeyVaultConfig hashicorpKeyVaultConfig) {
    this.passwordFile = passwordFile;
    this.passwords = passwords;
    this.keyData = keyData;
    this.azureKeyVaultConfig = azureKeyVaultConfig;
    this.hashicorpKeyVaultConfig = hashicorpKeyVaultConfig;

    if (null != azureKeyVaultConfig) {
      addKeyVaultConfig(azureKeyVaultConfig);
    }

    if (null != hashicorpKeyVaultConfig) {
      addKeyVaultConfig(hashicorpKeyVaultConfig);
    }
  }

  public KeyConfiguration() {}

  public Path getPasswordFile() {
    return this.passwordFile;
  }

  public List<String> getPasswords() {
    return this.passwords;
  }

  public List<KeyData> getKeyData() {
    return this.keyData;
  }

  public AzureKeyVaultConfig getAzureKeyVaultConfig() {
    return this.azureKeyVaultConfig;
  }

  public HashicorpKeyVaultConfig getHashicorpKeyVaultConfig() {
    return hashicorpKeyVaultConfig;
  }

  public List<KeyVaultConfig> getKeyVaultConfigs() {
    if (keyVaultConfigs == null) {
      return null;
    }
    return keyVaultConfigs.stream().map(KeyVaultConfig.class::cast).collect(Collectors.toList());
  }

  public Optional<DefaultKeyVaultConfig> getKeyVaultConfig(KeyVaultType type) {
    if (type == null) {
      return Optional.empty();
    }

    if (KeyVaultType.AZURE.equals(type) && azureKeyVaultConfig != null) {
      return Optional.of(KeyVaultConfigConverter.convert(azureKeyVaultConfig));
    }

    if (KeyVaultType.HASHICORP.equals(type) && hashicorpKeyVaultConfig != null) {
      return Optional.of(KeyVaultConfigConverter.convert(hashicorpKeyVaultConfig));
    }

    if (keyVaultConfigs == null) {
      return Optional.empty();
    }

    return keyVaultConfigs.stream().filter(c -> type.equals(c.getKeyVaultType())).findFirst();
  }

  public void setPasswordFile(Path passwordFile) {
    this.passwordFile = passwordFile;
  }

  public void setPasswords(List<String> passwords) {
    this.passwords = passwords;
  }

  public void setKeyData(List<KeyData> keyData) {
    this.keyData = keyData;
  }

  public void addKeyVaultConfig(KeyVaultConfig keyVaultConfig) {
    if (keyVaultConfigs == null) {
      keyVaultConfigs = new ArrayList<>();
    }

    final DefaultKeyVaultConfig typedKeyVaultConfig;
    if (AzureKeyVaultConfig.class.isInstance(keyVaultConfig)) {
      typedKeyVaultConfig =
          KeyVaultConfigConverter.convert(AzureKeyVaultConfig.class.cast(keyVaultConfig));
    } else if (HashicorpKeyVaultConfig.class.isInstance(keyVaultConfig)) {
      typedKeyVaultConfig =
          KeyVaultConfigConverter.convert(HashicorpKeyVaultConfig.class.cast(keyVaultConfig));
    } else {
      typedKeyVaultConfig = DefaultKeyVaultConfig.class.cast(keyVaultConfig);
    }
    keyVaultConfigs.add(typedKeyVaultConfig);
  }
}
