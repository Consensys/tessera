package com.github.nexus.config;

import com.github.nexus.config.adapters.PrivateKeyFileAdapter;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class KeyData {

    @XmlElement
    @XmlJavaTypeAdapter(PrivateKeyFileAdapter.class)
    private final PrivateKey privateKey;

    @XmlElement
    private final PublicKey publicKey;

    public KeyData(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    private static KeyData create() {
        return new KeyData(null, null);
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

}
