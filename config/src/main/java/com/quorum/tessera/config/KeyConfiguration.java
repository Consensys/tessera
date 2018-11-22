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
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class KeyConfiguration extends ConfigItem {

    @ValidPath(checkExists = true, message = "Password file does not exist")
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private Path passwordFile;

    private List<String> passwords;

    @Valid
    @NotNull
    @Size(min = 1, message = "At least 1 public/private key pair must be provided")
    @XmlJavaTypeAdapter(KeyDataAdapter.class)
    private List<@Valid ConfigKeyPair> keyData;

    @Valid
    @XmlElement
    private KeyVaultConfig keyVaultConfig;

    public KeyConfiguration(final Path passwordFile, final List<String> passwords, final List<ConfigKeyPair> keyData, final KeyVaultConfig keyVaultConfig) {
        this.passwordFile = passwordFile;
        this.passwords = passwords;
        this.keyData = keyData;
        this.keyVaultConfig = keyVaultConfig;
    }

    public KeyConfiguration() {
    }

    public Path getPasswordFile() {
        return this.passwordFile;
    }

    public List<String> getPasswords() {
        return this.passwords;
    }

    public List<ConfigKeyPair> getKeyData() {
        return this.keyData;
    }

    public KeyVaultConfig getKeyVaultConfig() {
        return this.keyVaultConfig;
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

    public void setKeyVaultConfig(KeyVaultConfig keyVaultConfig) {
        this.keyVaultConfig = keyVaultConfig;
    }


}
