package com.quorum.tessera.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class KeyVaultConfig {
    private final String url;

    public KeyVaultConfig(String url) {
        this.url = url;
    }

    private static KeyVaultConfig create() {
        return new KeyVaultConfig(null);
    }

    public String getUrl() {
        return this.url;
    }

}
