package com.quorum.tessera.config;

import com.quorum.tessera.config.adapters.PathAdapter;
import com.quorum.tessera.config.constraints.ValidBase64;
import com.quorum.tessera.config.constraints.ValidKeyDataConfig;

import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Path;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class KeyData extends ConfigItem {

    @ValidKeyDataConfig
    @XmlElement
    private final KeyDataConfig config;

    @ValidBase64
    @Pattern(regexp = "^((?!NACL_FAILURE).)*$",
            message = "Could not decrypt the private key with the provided password, please double check the passwords provided")
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

    public KeyData(final KeyDataConfig keyDataConfig,
                   final String privateKey,
                   final String publicKey,
                   final Path privKeyPath,
                   final Path pubKeyPath) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.config = keyDataConfig;
        this.privateKeyPath = privKeyPath;
        this.publicKeyPath = pubKeyPath;
    }

    private static KeyData create() {
        return new KeyData(null, null, null, null, null);
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
}
