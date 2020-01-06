package com.quorum.tessera.config;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@Deprecated
@XmlAccessorType(XmlAccessType.FIELD)
public class AWSKeyVaultConfig extends ConfigItem implements KeyVaultConfig {

    @Valid
    @XmlAttribute
    @Pattern(regexp = "^https?://.+$", message = "must be a valid AWS service endpoint URL with scheme")
    private String endpoint;

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
