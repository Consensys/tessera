package com.quorum.tessera.config;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class Peer extends ConfigItem {

    @NotNull
    @XmlElement(required = true)
    private final String url;

    public Peer(String url) {
        this.url = url;
    }

    private Peer() {
        this(null);
    }

    private static Peer create() {
        return new Peer();
    }

    public String getUrl() {
        return url;
    }

}
