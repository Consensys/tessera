package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.adapters.PathAdapter;
import com.quorum.tessera.config.constraints.ValidUnsupportedKeyPair;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Path;

import static com.quorum.tessera.config.keypairs.ConfigKeyPairType.UNSUPPORTED;

@ValidUnsupportedKeyPair
public class UnsupportedKeyPair implements ConfigKeyPair {

    @XmlElement
    private final KeyDataConfig config;

    @XmlElement
    private final String privateKey;

    @XmlElement
    private final String publicKey;

    @XmlElement
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path privateKeyPath;

    @XmlElement
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path publicKeyPath;

    @XmlElement
    private final String azureVaultPublicKeyId;

    @XmlElement
    private final String azureVaultPrivateKeyId;

    public UnsupportedKeyPair(KeyDataConfig config, String privateKey, String publicKey, Path privateKeyPath, Path publicKeyPath, String azureVaultPublicKeyId, String azureVaultPrivateKeyId) {
        this.config = config;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.privateKeyPath = privateKeyPath;
        this.publicKeyPath = publicKeyPath;
        this.azureVaultPublicKeyId = azureVaultPublicKeyId;
        this.azureVaultPrivateKeyId = azureVaultPrivateKeyId;
    }

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

    @Override
    public void withPassword(String password) {

    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public KeyData marshal() {
        return null;
    }

    @Override
    public ConfigKeyPairType getType() {
        return UNSUPPORTED;
    }

}
