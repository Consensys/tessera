package com.quorum.tessera.config;

import org.hibernate.validator.constraints.URL;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class AWSKeyVaultConfig extends ConfigItem implements KeyVaultConfig {

    @Valid @XmlAttribute @URL private String endpoint;

    public AWSKeyVaultConfig(String endpoint) {
        this.endpoint = endpoint;
    }

    public AWSKeyVaultConfig() {}

    @Override
    public KeyVaultType getKeyVaultType() {
        return KeyVaultType.AWS;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public void setEndpoint(String url) {
        this.endpoint = url;
    }
}
