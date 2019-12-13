package com.quorum.tessera.config;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class AWSKeyVaultConfig extends ConfigItem implements KeyVaultConfig {

    @Valid
    @XmlAttribute
    @Pattern(
            regexp =
                    "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)")
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
