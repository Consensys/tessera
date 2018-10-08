package com.quorum.tessera.config.keypairs;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlElement;

public class AzureVaultKeyPair implements ConfigKeyPair {

    @NotNull
    @XmlElement
    @Pattern(regexp = "^[0-9a-zA-Z\\-]*$",
            message = "Azure Key Vault key IDs can only contain alphanumeric characters and dashes (-)")
    private String publicKeyId;

    @NotNull
    @XmlElement
    @Pattern(regexp = "^[0-9a-zA-Z\\-]*$",
            message = "Azure Key Vault key IDs can only contain alphanumeric characters and dashes (-)")
    private String privateKeyId;

    public AzureVaultKeyPair(String publicKeyId, String privateKeyId) {
        this.publicKeyId = publicKeyId;
        this.privateKeyId = privateKeyId;
    }

    public String getPublicKeyId() {
        return this.publicKeyId;
    }

    public String getPrivateKeyId() {
        return this.privateKeyId;
    }

    @Override
    public String getPublicKey() {
        //keys have not been fetched from vault yet so return null
        return null;
    }

    @Override
    public String getPrivateKey() {
        //keys have not been fetched from vault yet so return null
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
