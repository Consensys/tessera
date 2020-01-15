package com.quorum.tessera.config;

import com.quorum.tessera.config.adapters.KeyDataAdapter;
import com.quorum.tessera.config.adapters.PathAdapter;
import com.quorum.tessera.config.constraints.ValidPath;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    @XmlJavaTypeAdapter(KeyDataAdapter.class)
    private List<@Valid ConfigKeyPair> keyData;

    @Valid @XmlElement private List<@Valid DefaultKeyVaultConfig> keyVaultConfigs;

    @Valid @XmlElement private AzureKeyVaultConfig azureKeyVaultConfig;

    @Valid @XmlElement private HashicorpKeyVaultConfig hashicorpKeyVaultConfig;

    public KeyConfiguration(
            final Path passwordFile,
            final List<String> passwords,
            final List<ConfigKeyPair> keyData,
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

    public List<ConfigKeyPair> getKeyData() {
        return this.keyData;
    }

    public AzureKeyVaultConfig getAzureKeyVaultConfig() {
        return this.azureKeyVaultConfig;
    }

    public HashicorpKeyVaultConfig getHashicorpKeyVaultConfig() {
        return hashicorpKeyVaultConfig;
    }

    public List<DefaultKeyVaultConfig> getKeyVaultConfigs() {
        return keyVaultConfigs;
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

        return keyVaultConfigs.stream()
            .filter(c -> type.equals(c.getKeyVaultType()))
            .findFirst();
    }

    public void setPasswordFile(Path passwordFile) {
        this.passwordFile = passwordFile;
    }

    public void setPasswords(List<String> passwords) {
        this.passwords = passwords;
    }

    public void setKeyData(List<ConfigKeyPair> keyData) {
        this.keyData = keyData;
    }

    public void addKeyVaultConfig(KeyVaultConfig keyVaultConfig) {
        if (keyVaultConfigs == null) {
            keyVaultConfigs = new ArrayList<>();
        }

        if (KeyVaultType.AZURE.equals(keyVaultConfig.getKeyVaultType())) {
            keyVaultConfigs.add(KeyVaultConfigConverter.convert((AzureKeyVaultConfig) keyVaultConfig));
        } else if (KeyVaultType.HASHICORP.equals(keyVaultConfig.getKeyVaultType())) {
            keyVaultConfigs.add(KeyVaultConfigConverter.convert((HashicorpKeyVaultConfig) keyVaultConfig));
        } else {
            keyVaultConfigs.add((DefaultKeyVaultConfig) keyVaultConfig);
        }
    }
}
