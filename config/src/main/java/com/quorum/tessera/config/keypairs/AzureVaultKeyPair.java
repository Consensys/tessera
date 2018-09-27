package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.config.KeyData;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlElement;

import static com.quorum.tessera.config.keypairs.ConfigKeyPairType.AZURE;

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

    private ConfigKeyPairType type = AZURE;


    public AzureVaultKeyPair(String publicKeyId, String privateKeyId) {
        this.publicKeyId = publicKeyId;
        this.privateKeyId = privateKeyId;
    }

    @Override
    public KeyData marshal() {
        return new KeyData(null, null, null, null, null, privateKeyId, publicKeyId);
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

    @Override
    public ConfigKeyPairType getType() {
        return this.type;
    }
}
