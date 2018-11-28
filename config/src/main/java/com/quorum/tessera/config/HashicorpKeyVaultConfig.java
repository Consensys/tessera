package com.quorum.tessera.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;

public class HashicorpKeyVaultConfig extends ConfigItem implements KeyVaultConfig {

    @Valid
    @NotNull
    @XmlAttribute
    private String url;

    public HashicorpKeyVaultConfig(String url) {
        this.url = url;
    }

    public HashicorpKeyVaultConfig() {
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public KeyVaultType getKeyVaultType() {
        return KeyVaultType.HASHICORP;
    }
}
