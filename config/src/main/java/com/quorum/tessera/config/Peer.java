package com.quorum.tessera.config;

import com.quorum.tessera.config.constraints.ValidUrl;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
public class Peer extends ConfigItem {

    @ValidUrl
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null) return false;

        if (getClass() != obj.getClass()) return false;

        final Peer other = (Peer) obj;

        return Objects.equals(this.url, other.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.url);
    }
}
