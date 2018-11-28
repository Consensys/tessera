package com.quorum.tessera.config;

import com.quorum.tessera.config.adapters.PathAdapter;
import com.quorum.tessera.config.constraints.ValidBase64;
import com.quorum.tessera.config.constraints.ValidKeyDataConfig;

import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Path;

@XmlAccessorType(XmlAccessType.FIELD)
public class KeyData extends ConfigItem {

    @ValidKeyDataConfig
    @XmlElement
    private KeyDataConfig config;

    @ValidBase64
    @Pattern(regexp = "^((?!NACL_FAILURE).)*$",
            message = "Could not decrypt the private key with the provided password, please double check the passwords provided")
    @XmlElement
    private String privateKey;

    @XmlElement
    private String publicKey;

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

    @XmlElement
    private String hashicorpVaultPublicKeyId;

    @XmlElement
    private String hashicorpVaultPrivateKeyId;

    @XmlElement
    private String hashicorpVaultSecretPath;

    public KeyData(final KeyDataConfig keyDataConfig,
                   final String privateKey,
                   final String publicKey,
                   final Path privKeyPath,
                   final Path pubKeyPath,
                   final String azureVaultPrivateKeyId,
                   final String azureVaultPublicKeyId,
                   final String hashicorpVaultPrivateKeyId,
                   final String hashicorpVaultPublicKeyId,
                   final String hashicorpVaultSecretPath) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.config = keyDataConfig;
        this.privateKeyPath = privKeyPath;
        this.publicKeyPath = pubKeyPath;
        this.azureVaultPublicKeyId = azureVaultPublicKeyId;
        this.azureVaultPrivateKeyId = azureVaultPrivateKeyId;
        this.hashicorpVaultPublicKeyId = hashicorpVaultPublicKeyId;
        this.hashicorpVaultPrivateKeyId = hashicorpVaultPrivateKeyId;
        this.hashicorpVaultSecretPath = hashicorpVaultSecretPath;
    }

    public KeyData() {
    }


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

    public String getHashicorpVaultPublicKeyId() {
        return hashicorpVaultPublicKeyId;
    }

    public String getHashicorpVaultPrivateKeyId() {
        return hashicorpVaultPrivateKeyId;
    }

    public String getHashicorpVaultSecretPath() {
        return hashicorpVaultSecretPath;
    }
}
