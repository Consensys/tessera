package com.quorum.tessera.admin;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
class PublicKeyResponse {

    @XmlElement
    private String publicKey;

    PublicKeyResponse() {

    }

    void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
