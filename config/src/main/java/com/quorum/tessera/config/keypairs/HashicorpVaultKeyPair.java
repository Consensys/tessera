package com.quorum.tessera.config.keypairs;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;

public class HashicorpVaultKeyPair implements ConfigKeyPair {

    @NotNull
    @XmlElement
    private String publicKeyId;

    @NotNull
    @XmlElement
    private String privateKeyId;

    @NotNull
    @XmlElement
    private String secretPath;

    public HashicorpVaultKeyPair(String publicKeyId, String privateKeyId, String secretPath) {
        this.publicKeyId = publicKeyId;
        this.privateKeyId = privateKeyId;
        this.secretPath = secretPath;
    }

    public String getPublicKeyId() {
        return publicKeyId;
    }

    public String getPrivateKeyId() {
        return privateKeyId;
    }

    public String getSecretPath() {
        return secretPath;
    }

    @Override
    public String getPublicKey() {
        //keys are not fetched from vault yet so return null
        return null;
    }

    @Override
    public String getPrivateKey() {
        //keys are not fetched from vault yet so return null
        return null;
    }

    @Override
    public void withPassword(String password) {
        //password not used with vault stored keys
    }

    @Override
    public String getPassword() {
        //no password to return
        return "";
    }
}
