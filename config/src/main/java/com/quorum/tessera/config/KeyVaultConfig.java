package com.quorum.tessera.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class KeyVaultConfig extends ConfigItem {

    @Valid
    @NotNull
    @XmlAttribute
    private String url;

    public KeyVaultConfig(String url) {
        this.url = url;
    }

    public KeyVaultConfig() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }




}
