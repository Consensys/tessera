package com.quorum.tessera.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class KeyVaultConfig extends ConfigItem {

    @Valid
    @NotNull(message = "{KeyVaultConfig.typeCannotBeNull.message}")
    @XmlAttribute
    private KeyVaultType vaultType;

    @Valid
    @NotNull
    @XmlAttribute
    private String url;

    public KeyVaultConfig(KeyVaultType vaultType, String url) {
        this.vaultType = vaultType;
        this.url = url;
    }

    public KeyVaultConfig() {
    }

    public KeyVaultType getVaultType() {
        return this.vaultType;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setKeyVaultType(KeyVaultType keyVaultType) {
        this.vaultType = keyVaultType;
    }


}
