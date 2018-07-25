package com.github.tessera.config;

import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class KeyData extends ConfigItem {

    @XmlElement
    private final KeyDataConfig config;

    @XmlElement
    @XmlSchemaType(name = "anyURI")
    private final String privateKey;

    @XmlElement
    @XmlSchemaType(name = "anyURI")
    private final String publicKey;

    public KeyData(KeyDataConfig keyDataConfig, String privateKey, String publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.config = keyDataConfig;
    }

    private static KeyData create() {
        return new KeyData(null, null, null);
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public KeyDataConfig getConfig() {
        return config;
    }

    public boolean hasKeys() {
        return Objects.nonNull(privateKey) && Objects.nonNull(publicKey);
    }

}
