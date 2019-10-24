package com.quorum.tessera.config;

import com.quorum.tessera.config.adapters.MapAdapter;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class EncryptorConfig extends ConfigItem {
    
    @XmlElement
    private EncryptorType type;


    @XmlJavaTypeAdapter(MapAdapter.class)
    @XmlElement(name="properties")
    private Map<String,String> properties;
    
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
 
}
