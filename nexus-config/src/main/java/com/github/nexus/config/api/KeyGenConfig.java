package com.github.nexus.config.api;

import java.nio.file.Path;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import com.github.nexus.config.jaxb.PathAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KeyGenConfig", propOrder = {
    "basePath"
})
public class KeyGenConfig
    implements com.github.nexus.config.KeyGenConfig
{

    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private Path basePath;
    
    @XmlAttribute(name = "generateIfMissing")
    private Boolean generateIfMissing;

    @Override
    public Path getBasePath() {
        return basePath;
    }

    public void setBasePath(Path value) {
        this.basePath = value;
    }

    @Override
    public boolean isGenerateIfMissing() {
        if (generateIfMissing == null) {
            return false;
        } else {
            return generateIfMissing;
        }
    }

    public void setGenerateIfMissing(Boolean value) {
        this.generateIfMissing = value;
    }

}
