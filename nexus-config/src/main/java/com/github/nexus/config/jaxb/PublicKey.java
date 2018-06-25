package com.github.nexus.config.jaxb;

import java.nio.file.Path;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PublicKey", propOrder = {
    "path",
    "value"
})
public class PublicKey
        implements com.github.nexus.config.PublicKey {

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private Path path;
    
    @XmlSchemaType(name = "anyURI")
    private String value;

    @Override
    public Path getPath() {
        return path;
    }

    public void setPath(Path value) {
        this.path = value;
    }


    @Override
    public String getValue() {
        return value;
    }


    public void setValue(String value) {
        this.value = value;
    }

}
