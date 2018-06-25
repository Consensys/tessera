package com.github.nexus.config;

import java.nio.file.Path;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class PublicKey {

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path path;
    
    @XmlSchemaType(name = "anyURI")
    private final String value;

    public PublicKey(Path path, String value) {
        this.path = path;
        this.value = value;
    }
    
    private static PublicKey create() {
        return new PublicKey(null,null);
    }

    public Path getPath() {
        return path;
    }

    public String getValue() {
        return value;
    }

}
