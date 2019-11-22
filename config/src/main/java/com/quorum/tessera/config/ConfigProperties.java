package com.quorum.tessera.config;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class ConfigProperties {

    @XmlAnyElement
    private List<JAXBElement<String>> properties = new ArrayList<>();

    public ConfigProperties() {
    }

    public List<JAXBElement<String>> getProperties() {
        return properties;
    }

    public void setProperties(List<JAXBElement<String>> properties) {
        this.properties = properties;
    }
}
