package com.github.nexus.config.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KeyData", propOrder = {
    "privateKey",
    "publicKey"
})
public class KeyData implements com.github.nexus.config.KeyData {

    @XmlElement(required = true)
    private PrivateKey privateKey;

    @XmlElement(required = true)
    private PublicKey publicKey;

    @Override
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    @Override
    public PublicKey getPublicKey() {
        return publicKey;
    }

}
