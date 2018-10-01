package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.constraints.ValidBase64;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;

public class DirectKeyPair implements ConfigKeyPair {

    @NotNull
    @ValidBase64(message = "Invalid Base64 key provided")
    @XmlElement
    private final String publicKey;

    @NotNull
    @ValidBase64(message = "Invalid Base64 key provided")
    @XmlElement
    private final String privateKey;

    public DirectKeyPair(final String publicKey, final String privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @Override
    public KeyData marshal() {
        return new KeyData(null, this.privateKey, this.publicKey, null, null);
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
        //no need to keep a password for this key type
    }

    @Override
    public String getPassword() {
        //no password to return
        return "";
    }

}
