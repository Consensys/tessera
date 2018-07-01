package com.github.nexus.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class KeyData {

    @NotNull
    @Valid
    @XmlElement(required = true)
    private final PrivateKey privateKey;

    @NotNull
    @Valid
    @XmlElement(required = true)
    private final PublicKey publicKey;

    public KeyData(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    private static KeyData create() {
        return new KeyData();
    }

    private KeyData() {
        this(null, null);
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

}
