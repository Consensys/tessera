package com.quorum.tessera.config;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Peer extends ConfigItem {

    @NotNull
    @XmlElement(required = true)
    private String url;

    public Peer(String url) {
        this.url = url;
    }

    public Peer() {}

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
