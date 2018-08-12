package com.quorum.tessera.config;

import com.quorum.tessera.config.adapters.PathAdapter;
import com.quorum.tessera.config.constraints.ValidKeyDataConfig;

import java.nio.file.Path;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class KeyData extends ConfigItem {

    @ValidKeyDataConfig(message = "A locked key was provided without a password.\n" +
                     "Please ensure the same number of passwords are provided as there are keys and remember to include empty passwords for unlocked keys")
    @XmlElement
    private final KeyDataConfig config;

    @NotNull
    @XmlElement
    @XmlSchemaType(name = "anyURI")
    private final String privateKey;

    @NotNull
    @XmlElement
    @XmlSchemaType(name = "anyURI")
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

    public boolean hasKeys() {
        return Objects.nonNull(privateKey) && Objects.nonNull(publicKey);
    }

}
