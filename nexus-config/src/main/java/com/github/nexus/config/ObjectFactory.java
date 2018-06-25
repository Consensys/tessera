package com.github.nexus.config;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {

    private static final QName QNAME = new QName("http://nexus.github.com/config", "configuration");

    public ObjectFactory() {
    }


    @XmlElementDecl(namespace = "http://nexus.github.com/config", name = "configuration")
    public JAXBElement<Config> createConfiguration(Config value) {
        return new JAXBElement<>(QNAME, Config.class, null, value);
    }

}
