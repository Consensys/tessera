package com.github.tessera.config;

import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class KeyData {

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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.config);
        hash = 53 * hash + Objects.hashCode(this.privateKey);
        hash = 53 * hash + Objects.hashCode(this.publicKey);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final KeyData other = (KeyData) obj;
        if (!Objects.equals(this.privateKey, other.privateKey)) {
            return false;
        }
        if (!Objects.equals(this.publicKey, other.publicKey)) {
            return false;
        }
        if (!Objects.equals(this.config, other.config)) {
            return false;
        }
        return true;
    }

}
