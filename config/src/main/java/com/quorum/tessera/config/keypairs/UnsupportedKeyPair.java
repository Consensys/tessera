package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.adapters.PathAdapter;
import com.quorum.tessera.config.constraints.ValidUnsupportedKeyPair;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Path;

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

    public UnsupportedKeyPair(KeyDataConfig config, String privateKey, String publicKey, Path privateKeyPath, Path publicKeyPath) {
        this.config = config;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.privateKeyPath = privateKeyPath;
        this.publicKeyPath = publicKeyPath;
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

    @Override
    public void withPassword(String password) {
        //do nothing as password not used with this keypair type
    }

    @Override
    public String getPassword() {
        return null;
    }

}
