package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.config.constraints.ValidBase64;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;

public class DirectKeyPair implements ConfigKeyPair {

    @Size(min = 1)
    @NotNull
    @ValidBase64(message = "Invalid Base64 key provided")
    @XmlElement
    private final String publicKey;

    @Size(min = 1)
    @NotNull
    @ValidBase64(message = "Invalid Base64 key provided")
    @XmlElement
    private final String privateKey;

    public DirectKeyPair(final String publicKey, final String privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @Override
    public String getPublicKey() {
        return this.publicKey;
    }

    @Override
    public String getPrivateKey() {
        return this.privateKey;
    }

    @Override
    public void withPassword(final String password) {
        // no need to keep a password for this key type
    }

    @Override
    public String getPassword() {
        // no password to return
        return "";
    }
}
