package com.quorum.tessera.config;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {

    private static final QName QNAME = new QName("http://tessera.github.com/config", "configuration");

    @XmlElementDecl(namespace = "http://tessera.github.com/config", name = "configuration")
    public JAXBElement<Config> createConfiguration(Config value) {
        return new JAXBElement<>(QNAME, Config.class, null, value);
    }

}
