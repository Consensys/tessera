package com.quorum.tessera.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@Deprecated
@XmlAccessorType(XmlAccessType.FIELD)
public class AzureKeyVaultConfig extends ConfigItem implements KeyVaultConfig {

    @Valid @NotNull @XmlAttribute private String url;

    public AzureKeyVaultConfig(String url) {
        this.url = url;
    }

    public AzureKeyVaultConfig() {}

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public KeyVaultType getKeyVaultType() {
        return KeyVaultType.AZURE;
    }
}
