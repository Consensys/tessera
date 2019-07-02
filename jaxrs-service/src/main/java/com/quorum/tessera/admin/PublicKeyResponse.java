package com.quorum.tessera.admin;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
class PublicKeyResponse {

    @XmlElement private String publicKey;

    // No args constructor required for jaxb marshalling
    private PublicKeyResponse() {}

    PublicKeyResponse(String publicKey) {
        this.publicKey = publicKey;
    }

    String getPublicKey() {
        return this.publicKey;
    }
}
