package com.github.tessera.config;

import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class Peer {

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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + Objects.hashCode(this.url);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Peer other = (Peer) obj;
        if (!Objects.equals(this.url, other.url)) {
            return false;
        }
        return true;
    }

}
