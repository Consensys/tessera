package com.quorum.tessera.config;

import com.quorum.tessera.config.adapters.MapAdapter;
import java.util.Map;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class EncryptorConfig {

    @XmlAttribute private EncryptorType type;

    @XmlJavaTypeAdapter(MapAdapter.class)
    @XmlElement
    private Map<String, String> properties;

    public EncryptorType getType() {
        return type;
    }

    public void setType(EncryptorType type) {
        this.type = type;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.type);
        hash = 61 * hash + Objects.hashCode(this.properties);
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
        final EncryptorConfig other = (EncryptorConfig) obj;
        if (this.type != other.type) {
            return false;
        }
        if (!Objects.equals(this.properties, other.properties)) {
            return false;
        }
        return true;
    }
}
