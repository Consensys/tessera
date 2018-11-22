package com.quorum.tessera.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class KeyVaultConfig extends ConfigItem {

    @Valid
    @NotNull(message = "{KeyVaultConfig.typeCannotBeNull.message}")
    @XmlAttribute
    private final KeyVaultType vaultType;

    @Valid
    @NotNull
    @XmlAttribute
    private final String url;

    public KeyVaultConfig(KeyVaultType vaultType, String url) {
        this.vaultType = vaultType;
        this.url = url;
    }

    private static KeyVaultConfig create() {
        return new KeyVaultConfig(null, null);
    }

    public KeyVaultType getVaultType() {
        return vaultType;
    }

    public String getUrl() {
        return url;
    }
}
